/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.model;

import guru.nidi.codeassert.AnalyzerException;

import java.io.IOException;

class JavaClassImportBuilder {
    private static final char CLASS_DESCRIPTOR = 'L';
    private static final char TYPE_END = ';';

    private final JavaClass jClass;
    private final ConstantPool constantPool;

    public JavaClassImportBuilder(JavaClass jClass, ConstantPool constantPool) {
        this.jClass = jClass;
        this.constantPool = constantPool;
    }

    public void addClassName(String className) {
        jClass.setType(className);
    }

    public void addSuperClass(String className) {
        addImport(className);
    }

    public void addInterfaces(String[] interfaceNames) {
        for (final String interfaceName : interfaceNames) {
            addImport(interfaceName);
        }
    }

    public void addClassConstantReferences() throws IOException {
        for (final Constant constant : constantPool) {
            if (constant.tag == Constant.CLASS) {
                final String name = constantPool.getUtf8(constant.nameIndex);
                addImport(name);
            }
        }
    }

    public void addMethodRefs(MemberInfo[] methods) throws IOException {
        addMemberAnnotationRefs(methods);
        addMemberSignatureRefs(SignatureParser.Source.METHOD, methods);
        addMemberTypes(methods);
    }

    public void addFieldRefs(MemberInfo[] fields) throws IOException {
        addMemberAnnotationRefs(fields);
        addMemberSignatureRefs(SignatureParser.Source.FIELD, fields);
        addMemberTypes(fields);
    }

    private void addMemberAnnotationRefs(MemberInfo[] info) throws IOException {
        for (int j = 1; j < info.length; j++) {
            if (info[j].annotations != null) {
                addAnnotationReferences(info[j].annotations);
            }
        }
    }

    private void addMemberSignatureRefs(SignatureParser.Source source, MemberInfo[] infos) throws IOException {
        for (final MemberInfo info : infos) {
            if (info.signature != null) {
                final String name = constantPool.getUtf8(u2(info.signature.value, 0));
                for (final String clazz : SignatureParser.parseSignature(source, name).getClasses()) {
                    addImport(clazz);
                }
            }
        }
    }

    private void addMemberTypes(MemberInfo[] infos) throws IOException {
        for (final MemberInfo info : infos) {
            final String descriptor = constantPool.getUtf8(info.descriptorIndex);
            final String[] types = descriptorToTypes(descriptor);
            for (final String type : types) {
                if (type.length() > 0) {
                    addImport(type);
                }
            }
        }
    }

    public void addAttributeRefs(AttributeInfo[] attributes) throws IOException {
        addAttributeAnnotationRefs(attributes);
        addAttributeSignatureRefs(attributes);
    }

    private void addAttributeSignatureRefs(AttributeInfo[] attributes) throws IOException {
        for (final AttributeInfo attr : attributes) {
            if (attr.isSignature()) {
                final String name = constantPool.getUtf8(u2(attr.value, 0));
                for (final String clazz : SignatureParser.parseSignature(SignatureParser.Source.CLASS, name).getClasses()) {
                    addImport(clazz);
                }
            }
        }
    }

    private void addAttributeAnnotationRefs(AttributeInfo[] attributes) throws IOException {
        for (int j = 1; j < attributes.length; j++) {
            if (attributes[j].isAnnotation()) {
                addAnnotationReferences(attributes[j]);
            }
        }
    }

    private void addAnnotationReferences(AttributeInfo annotation) throws IOException {
        // JVM Spec 4.8.15
        final byte[] data = annotation.value;
        final int numAnnotations = u2(data, 0);
        addAnnotationReferences(data, 2, numAnnotations);
    }

    private int addAnnotationReferences(byte[] data, int index, int numAnnotations) throws IOException {
        int i = index;
        for (int a = 0; a < numAnnotations; a++) {
            final int typeIndex = u2(data, i);
            final int elements = u2(data, i += 2);
            addImport(descriptorToType(constantPool.getUtf8(typeIndex)));
            i += 2;
            for (int e = 0; e < elements; e++) {
                i = addAnnotationElementValueReferences(data, i + 2);
            }
        }
        return i;
    }

    private int addAnnotationElementValueReferences(byte[] data, int i) throws IOException {
        final byte tag = data[i];
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                return i + 3;
            case 'e':
                final int enumTypeIndex = u2(data, i + 1);
                addImport(descriptorToType(constantPool.getUtf8(enumTypeIndex)));
                return i + 5;
            case 'c':
                final int classInfoIndex = u2(data, i + 1);
                addImport(descriptorToType(constantPool.getUtf8(classInfoIndex)));
                return i + 3;
            case '@':
                return addAnnotationReferences(data, i + 1, 1);
            case '[':
                final int numValues = u2(data, i + 1);
                int k = i + 3;
                for (int j = 0; j < numValues; j++) {
                    k = addAnnotationElementValueReferences(data, k);
                }
                return k;
            default:
                throw new AnalyzerException("Unknown tag '" + tag + "'");
        }
    }

    private int u2(byte[] data, int index) {
        return (data[index] << 8 & 0xFF00) | (data[index + 1] & 0xFF);
    }

    private void addImport(String type) {
        final String name = getTypeName(type);
        if (name != null) {
            jClass.addImport(name);
        }
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    private String getTypeName(String s) {
        final String typed;
        if (s.length() > 0 && s.charAt(0) == '[') {
            final String types[] = descriptorToTypes(s);
            if (types.length == 0) {
                return null; // primitives
            }
            typed = types[0];
        } else {
            typed = s;
        }
        return slashesToDots(typed);
    }

    private String descriptorToType(String descriptor) {
        if (!descriptor.startsWith("L")) {
            throw new AssertionError("Expected Object descriptor, but found '" + descriptor + "'");
        }
        return descriptor.substring(1, descriptor.length() - 1);
    }

    private String[] descriptorToTypes(String descriptor) {
        int typesCount = 0;
        for (int i = 0; i < descriptor.length(); i++) {
            if (descriptor.charAt(i) == TYPE_END) {
                typesCount++;
            }
        }

        final String types[] = new String[typesCount];

        int typeIndex = 0;
        for (int index = 0; index < descriptor.length(); index++) {
            final int startIndex = descriptor.indexOf(CLASS_DESCRIPTOR, index);
            if (startIndex < 0) {
                break;
            }
            index = descriptor.indexOf(TYPE_END, startIndex + 1);
            types[typeIndex++] = descriptor.substring(startIndex + 1, index);
        }

        return types;
    }
}
