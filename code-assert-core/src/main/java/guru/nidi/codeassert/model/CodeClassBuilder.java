/*
 * Copyright © 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;

class CodeClassBuilder {
    private static final char CLASS_DESCRIPTOR = 'L';
    private static final char TYPE_END = ';';

    final CodeClass clazz;
    private final Model model;
    private final ConstantPool constantPool;

    private CodeClassBuilder(CodeClass clazz, Model model, ConstantPool constantPool) {
        this.clazz = clazz;
        this.model = model;
        this.constantPool = constantPool;
    }

    CodeClassBuilder(String className, Model model, ConstantPool constantPool) {
        this(model.getOrCreateClass(className), model, constantPool);
    }

    CodeClassBuilder(CodeClass clazz) {
        this(clazz, null, null);
    }

    CodeClassBuilder addSuperClass(String className) {
        clazz.superClass = className;
        addImport(className);
        return this;
    }

    CodeClassBuilder addInterfaces(List<String> interfaceNames) {
        for (final String interfaceName : interfaceNames) {
            addImport(interfaceName);
        }
        return this;
    }

    CodeClassBuilder addClassConstantReferences() throws IOException {
        for (final Constant constant : constantPool) {
            if (constant.tag == Constant.CLASS) {
                final String name = constantPool.getUtf8(constant.nameIndex);
                addImport(name);
            }
        }
        return this;
    }

    CodeClassBuilder addFlags(int flags) {
        clazz.flags = flags;
        return this;
    }

    CodeClassBuilder addMethodRefs(List<MemberInfo> methods) throws IOException {
        addMemberAnnotationRefs(methods);
        addMemberSignatureRefs(SignatureParser.Source.METHOD, methods);
        addMemberTypes(methods);
        clazz.methods.addAll(methods);
        return this;
    }

    CodeClassBuilder addFieldRefs(List<MemberInfo> fields) throws IOException {
        addMemberAnnotationRefs(fields);
        addMemberSignatureRefs(SignatureParser.Source.FIELD, fields);
        addMemberTypes(fields);
        clazz.fields.addAll(fields);
        return this;
    }

    CodeClassBuilder addAttributeRefs(List<AttributeInfo> attributes) throws IOException {
        for (final AttributeInfo attribute : attributes) {
            addSourceAttribute(attribute);
            addAttributeAnnotationRefs(attribute);
            addAttributeSignatureRefs(attribute);
        }
        return this;
    }

    CodeClassBuilder addPackageInfo(Model model, String className) {
        if (className.endsWith(".package-info")) {
            final CodePackage pack = model.getOrCreatePackage(model.packageOf(className));
            for (final CodeClass ann : clazz.getAnnotations()) {
                pack.addAnnotation(ann);
            }
        }
        return this;
    }

    CodeClassBuilder addCodeSizes(int totalSize, List<MemberInfo> methods) {
        int codeSize = 0;
        for (final MemberInfo method : methods) {
            codeSize += method.codeSize;
        }
        clazz.codeSize = codeSize;
        clazz.totalSize = totalSize;
        return this;
    }

    CodeClassBuilder addSourceSizes(int sourceSize,
                                    int codeLines, int commentLines, int emptyLines, int totalLines) {
        clazz.sourceSize = sourceSize;
        clazz.codeLines = codeLines;
        clazz.commentLines = commentLines;
        clazz.emptyLines = emptyLines;
        clazz.totalLines = totalLines;
        return this;
    }

    private void addMemberAnnotationRefs(List<MemberInfo> members) throws IOException {
        for (final MemberInfo member : members) {
            if (member.annotations != null) {
                addAnnotationReferences(member.annotations, member);
            }
        }
    }

    private void addMemberSignatureRefs(SignatureParser.Source source, List<MemberInfo> members) throws IOException {
        for (final MemberInfo member : members) {
            if (member.signature != null) {
                for (final String clazz : SignatureParser.parseSignature(source, member.signature).getClasses()) {
                    addMemberClassRef(member, clazz);
                    addImport(clazz);
                }
            }
        }
    }

    private void addMemberTypes(List<MemberInfo> members) {
        for (final MemberInfo member : members) {
            final String[] types = descriptorToTypes(member.descriptor);
            for (final String type : types) {
                if (type.length() > 0) {
                    addMemberClassRef(member, type);
                    addImport(type);
                }
            }
        }
    }

    private void addSourceAttribute(AttributeInfo attribute) throws IOException {
        if (attribute.isSource()) {
            clazz.sourceFile = attribute.sourceFile(constantPool);
        }
    }

    private void addAttributeSignatureRefs(AttributeInfo attribute) throws IOException {
        if (attribute.isSignature()) {
            final String name = constantPool.getUtf8(attribute.u2(0));
            for (final String clazz : SignatureParser.parseSignature(SignatureParser.Source.CLASS, name).getClasses()) {
                addImport(clazz);
            }
        }
    }

    private void addAttributeAnnotationRefs(AttributeInfo attribute) throws IOException {
        if (attribute.isAnnotation()) {
            addAnnotationReferences(attribute, null);
        }
    }

    private void addAnnotationReferences(AttributeInfo annotation, MemberInfo member) throws IOException {
        // JVM Spec 4.8.15
        addAnnotationReferences(annotation, member, 2, annotation.u2(0));
    }

    private int addAnnotationReferences(AttributeInfo annotation, MemberInfo member, int index, int numAnnotations)
            throws IOException {
        int i = index;
        for (int a = 0; a < numAnnotations; a++) {
            final int typeIndex = annotation.u2(i);
            i += 2;
            final int elements = annotation.u2(i);
            i += 2;
            final String annType = getTypeName(descriptorToType(constantPool.getUtf8(typeIndex)));
            clazz.addAnnotation(annType, model, member == null ? clazz.getAnnotations() : member.annotationClasses);
            for (int e = 0; e < elements; e++) {
                i = addAnnotationElementValueReferences(annotation, member, i + 2);
            }
        }
        return i;
    }

    private int addAnnotationElementValueReferences(AttributeInfo annotation, MemberInfo member, int i)
            throws IOException {
        final byte tag = annotation.value[i];
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
                addClassOrEnumRef(annotation, member, i);
                return i + 5;
            case 'c':
                addClassOrEnumRef(annotation, member, i);
                return i + 3;
            case '@':
                return addAnnotationReferences(annotation, member, i + 1, 1);
            case '[':
                final int numValues = annotation.u2(i + 1);
                int k = i + 3;
                for (int j = 0; j < numValues; j++) {
                    k = addAnnotationElementValueReferences(annotation, member, k);
                }
                return k;
            default:
                throw new AnalyzerException("Unknown tag '" + tag + "'");
        }
    }

    private void addClassOrEnumRef(AttributeInfo annotation, MemberInfo member, int i) throws IOException {
        final int enumTypeIndex = annotation.u2(i + 1);
        final String type = descriptorToType(constantPool.getUtf8(enumTypeIndex));
        addMemberClassRef(member, type);
        addImport(type);
    }

    private void addImport(String type) {
        final String name = getTypeName(type);
        if (name != null) {
            clazz.addImport(name, model);
        }
    }

    private void addMemberClassRef(MemberInfo member, String type) {
        if (member != null) {
            final String name = getTypeName(type);
            if (name != null) {
                member.referencedClasses.add(name);
            }
        }
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    private String getTypeName(String s) {
        final String typed;
        if (s.length() > 0 && s.charAt(0) == '[') {
            final String[] types = descriptorToTypes(s);
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

        final String[] types = new String[typesCount];

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
