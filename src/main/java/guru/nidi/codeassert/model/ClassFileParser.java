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

import java.io.*;

/**
 * The <code>ClassFileParser</code> class is responsible for
 * parsing a Java class file to create a <code>JavaClass</code>
 * instance.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

class ClassFileParser {

    private static final int JAVA_MAGIC = 0xCAFEBABE;
    private static final int CONSTANT_UTF8 = 1;
    private static final int CONSTANT_UNICODE = 2;
    private static final int CONSTANT_INTEGER = 3;
    private static final int CONSTANT_FLOAT = 4;
    private static final int CONSTANT_LONG = 5;
    private static final int CONSTANT_DOUBLE = 6;
    private static final int CONSTANT_CLASS = 7;
    private static final int CONSTANT_STRING = 8;
    private static final int CONSTANT_FIELD = 9;
    private static final int CONSTANT_METHOD = 10;
    private static final int CONSTANT_INTERFACEMETHOD = 11;
    private static final int CONSTANT_NAMEANDTYPE = 12;

    private static final int CONSTANT_METHOD_HANDLE = 15;
    private static final int CONSTANT_METHOD_TYPE = 16;
    private static final int CONSTANT_INVOKEDYNAMIC = 18;

    private static final char CLASS_DESCRIPTOR = 'L';
    private static final int ACC_INTERFACE = 0x200;
    private static final int ACC_ABSTRACT = 0x400;

    private static final String ATTR_ANNOTATIONS = "RuntimeVisibleAnnotations";
    private static final String ATTR_SIGNATURE = "Signature";
    private static final String ATTR_SOURCE = "SourceFile";

    private final PackageCollector collector;
    private String className;
    private String superClassName;
    private String interfaceNames[];
    private JavaClass jClass;
    private Constant[] constantPool;
    private FieldOrMethodInfo[] fields;
    private FieldOrMethodInfo[] methods;
    private AttributeInfo[] attributes;
    private DataInputStream in;


    public ClassFileParser() {
        this(PackageCollector.all());
    }

    public ClassFileParser(PackageCollector collector) {
        this.collector = collector;
        reset();
    }

    private void reset() {
        className = null;
        superClassName = null;
        interfaceNames = new String[0];

        jClass = null;
        constantPool = new Constant[1];
        fields = new FieldOrMethodInfo[0];
        methods = new FieldOrMethodInfo[0];
        attributes = new AttributeInfo[0];
    }

    public JavaClass parse(File file) throws IOException {
        try (final InputStream in = new FileInputStream(file)) {
            return parse(in);
        }
    }

    public JavaClass parse(InputStream is) throws IOException {
        reset();
        jClass = new JavaClass("Unknown");
        in = new DataInputStream(is);

        parseMagic();
        parseMinorVersion();
        parseMajorVersion();

        constantPool = parseConstantPool();

        parseAccessFlags();

        className = parseClassName();
        superClassName = parseSuperClassName();
        interfaceNames = parseInterfaces();
        fields = parseFields();
        methods = parseMethods();

        parseAttributes();

        addSignatureReferences();
        addClassConstantReferences();
        addAnnotationsReferences();

        return jClass;
    }

    private int parseMagic() throws IOException {
        int magic = in.readInt();
        if (magic != JAVA_MAGIC) {
            throw new IOException("Invalid class file");
        }
        return magic;
    }

    private int parseMinorVersion() throws IOException {
        return in.readUnsignedShort();
    }

    private int parseMajorVersion() throws IOException {
        return in.readUnsignedShort();
    }

    private Constant[] parseConstantPool() throws IOException {
        int constantPoolSize = in.readUnsignedShort();
        Constant[] pool = new Constant[constantPoolSize];
        for (int i = 1; i < constantPoolSize; i++) {
            Constant constant = parseNextConstant();
            pool[i] = constant;

            // 8-byte constants use two constant pool entries
            if (constant.getTag() == CONSTANT_DOUBLE || constant.getTag() == CONSTANT_LONG) {
                i++;
            }
        }

        return pool;
    }

    private void parseAccessFlags() throws IOException {
        int accessFlags = in.readUnsignedShort();
    }

    private String parseClassName() throws IOException {
        int entryIndex = in.readUnsignedShort();
        String className = getClassConstantName(entryIndex);
        jClass.setName(className);
        jClass.setPackageName(getPackageName(className));

        return className;
    }

    private String parseSuperClassName() throws IOException {
        int entryIndex = in.readUnsignedShort();
        String superClassName = getClassConstantName(entryIndex);
        addImport(getPackageName(superClassName));

        return superClassName;
    }

    private String[] parseInterfaces() throws IOException {
        int interfacesCount = in.readUnsignedShort();
        String[] interfaceNames = new String[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            int entryIndex = in.readUnsignedShort();
            interfaceNames[i] = getClassConstantName(entryIndex);
            addImport(getPackageName(interfaceNames[i]));
        }

        return interfaceNames;
    }

    private FieldOrMethodInfo[] parseFields() throws IOException {
        int fieldsCount = in.readUnsignedShort();
        FieldOrMethodInfo[] fields = new FieldOrMethodInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            fields[i] = parseFieldOrMethodInfo();
            String descriptor = toUTF8(fields[i].getDescriptorIndex());
            String[] types = descriptorToTypes(descriptor);
            for (String type : types) {
                addImport(getPackageName(type));
            }
        }

        return fields;
    }

    private FieldOrMethodInfo[] parseMethods() throws IOException {
        int methodsCount = in.readUnsignedShort();
        FieldOrMethodInfo[] methods = new FieldOrMethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            methods[i] = parseFieldOrMethodInfo();
            String descriptor = toUTF8(methods[i].getDescriptorIndex());
            String[] types = descriptorToTypes(descriptor);
            for (String type : types) {
                if (type.length() > 0) {
                    addImport(getPackageName(type));
                }
            }
        }

        return methods;
    }

    private Constant parseNextConstant() throws IOException {
        Constant result;
        byte tag = in.readByte();

        switch (tag) {
            case (ClassFileParser.CONSTANT_CLASS):
            case (ClassFileParser.CONSTANT_STRING):
            case (ClassFileParser.CONSTANT_METHOD_TYPE):
                result = new Constant(tag, in.readUnsignedShort());
                break;
            case (ClassFileParser.CONSTANT_FIELD):
            case (ClassFileParser.CONSTANT_METHOD):
            case (ClassFileParser.CONSTANT_INTERFACEMETHOD):
            case (ClassFileParser.CONSTANT_NAMEANDTYPE):
            case (ClassFileParser.CONSTANT_INVOKEDYNAMIC):
                result = new Constant(tag, in.readUnsignedShort(), in.readUnsignedShort());
                break;
            case (ClassFileParser.CONSTANT_INTEGER):
                result = new Constant(tag, new Integer(in.readInt()));
                break;
            case (ClassFileParser.CONSTANT_FLOAT):
                result = new Constant(tag, in.readFloat());
                break;
            case (ClassFileParser.CONSTANT_LONG):
                result = new Constant(tag, in.readLong());
                break;
            case (ClassFileParser.CONSTANT_DOUBLE):
                result = new Constant(tag, in.readDouble());
                break;
            case (ClassFileParser.CONSTANT_UTF8):
                result = new Constant(tag, in.readUTF());
                break;
            case (ClassFileParser.CONSTANT_METHOD_HANDLE):
                result = new Constant(tag, in.readByte(), in.readUnsignedShort());
                break;
            default:
                throw new IOException("Unknown constant: " + tag);
        }

        return result;
    }

    private FieldOrMethodInfo parseFieldOrMethodInfo() throws IOException {
        FieldOrMethodInfo result = new FieldOrMethodInfo(in.readUnsignedShort(), in.readUnsignedShort(), in.readUnsignedShort());

        int attributesCount = in.readUnsignedShort();
        for (int a = 0; a < attributesCount; a++) {
            AttributeInfo attribute = parseAttribute();
            if (ATTR_ANNOTATIONS.equals(attribute.name)) {
                result._runtimeVisibleAnnotations = attribute;
            }
            if (ATTR_SIGNATURE.equals(attribute.name)) {
                result._signature = attribute;
            }
        }

        return result;
    }

    private void parseAttributes() throws IOException {
        int attributesCount = in.readUnsignedShort();
        attributes = new AttributeInfo[attributesCount];

        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = parseAttribute();

            // Section 4.7.7 of VM Spec - Class File Format
            if (attributes[i].getName() != null) {
                if (attributes[i].getName().equals(ATTR_SOURCE)) {
                    byte[] b = attributes[i].getValue();
                    int b0 = b[0] < 0 ? b[0] + 256 : b[0];
                    int b1 = b[1] < 0 ? b[1] + 256 : b[1];
                    int pe = b0 * 256 + b1;

                    String descriptor = toUTF8(pe);
                    jClass.setSourceFile(descriptor);
                }
            }
        }
    }

    private AttributeInfo parseAttribute() throws IOException {
        AttributeInfo result = new AttributeInfo();

        int nameIndex = in.readUnsignedShort();
        if (nameIndex != -1) {
            result.setName(toUTF8(nameIndex));
        }

        int attributeLength = in.readInt();
        byte[] value = new byte[attributeLength];
        for (int b = 0; b < attributeLength; b++) {
            value[b] = in.readByte();
        }

        result.setValue(value);
        return result;
    }

    private Constant getConstantPoolEntry(int entryIndex) throws IOException {
        if (entryIndex < 0 || entryIndex >= constantPool.length) {
            throw new IOException("Illegal constant pool index : " + entryIndex);
        }

        return constantPool[entryIndex];
    }

    private void addSignatureReferences() throws IOException {
        for (AttributeInfo attr : attributes) {
            if (attr.getName().equals(ATTR_SIGNATURE)) {
                String name = toUTF8(u2(attr.getValue(), 0));
                for (final String pack : SignatureParser.parseClassSignature(name).getPackages()) {
                    addImport(pack);
                }
            }
        }
        for (FieldOrMethodInfo info : fields) {
            if (info._signature != null) {
                String name = toUTF8(u2(info._signature.getValue(), 0));
                for (final String pack : SignatureParser.parseFieldSignature(name).getPackages()) {
                    addImport(pack);
                }
            }
        }
        for (FieldOrMethodInfo info : methods) {
            if (info._signature != null) {
                String name = toUTF8(u2(info._signature.getValue(), 0));
                for (final String pack : SignatureParser.parseMethodSignature(name).getPackages()) {
                    addImport(pack);
                }
            }
        }
    }

    private void addClassConstantReferences() throws IOException {
        for (int j = 1; j < constantPool.length; j++) {
            if (constantPool[j].getTag() == CONSTANT_CLASS) {
                String name = toUTF8(constantPool[j].getNameIndex());
                addImport(getPackageName(name));
            }

            if (constantPool[j].getTag() == CONSTANT_DOUBLE || constantPool[j].getTag() == CONSTANT_LONG) {
                j++;
            }
        }
    }

    private void addAnnotationsReferences() throws IOException {
        for (int j = 1; j < attributes.length; j++) {
            if (ATTR_ANNOTATIONS.equals(attributes[j].name)) {
                addAnnotationReferences(attributes[j]);
            }
        }
        for (int j = 1; j < fields.length; j++) {
            if (fields[j]._runtimeVisibleAnnotations != null) {
                addAnnotationReferences(fields[j]._runtimeVisibleAnnotations);
            }
        }
        for (int j = 1; j < methods.length; j++) {
            if (methods[j]._runtimeVisibleAnnotations != null) {
                addAnnotationReferences(methods[j]._runtimeVisibleAnnotations);
            }
        }
    }

    private void addAnnotationReferences(AttributeInfo annotation) throws IOException {
        // JVM Spec 4.8.15
        byte[] data = annotation.value;
        int numAnnotations = u2(data, 0);
        int annotationIndex = 2;
        addAnnotationReferences(data, annotationIndex, numAnnotations);
    }

    private int addAnnotationReferences(byte[] data, int index, int numAnnotations) throws IOException {
        int visitedAnnotations = 0;
        while (visitedAnnotations < numAnnotations) {
            int typeIndex = u2(data, index);
            int numElementValuePairs = u2(data, index = index + 2);
            addImport(getPackageName(toUTF8(typeIndex).substring(1)));
            int visitedElementValuePairs = 0;
            index += 2;
            while (visitedElementValuePairs < numElementValuePairs) {
                index = addAnnotationElementValueReferences(data, index + 2);
                visitedElementValuePairs++;
            }
            visitedAnnotations++;
        }
        return index;
    }

    private int addAnnotationElementValueReferences(byte[] data, int index) throws IOException {
        byte tag = data[index];
        index += 1;
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
                index += 2;
                break;

            case 'e':
                int enumTypeIndex = u2(data, index);
                addImport(getPackageName(toUTF8(enumTypeIndex).substring(1)));
                index += 4;
                break;

            case 'c':
                int classInfoIndex = u2(data, index);
                addImport(getPackageName(toUTF8(classInfoIndex).substring(1)));
                index += 2;
                break;

            case '@':
                index = addAnnotationReferences(data, index, 1);
                break;

            case '[':
                int numValues = u2(data, index);
                index = index + 2;
                for (int i = 0; i < numValues; i++) {
                    index = addAnnotationElementValueReferences(data, index);
                }
                break;
        }
        return index;
    }

    private int u2(byte[] data, int index) {
        return (data[index] << 8 & 0xFF00) | (data[index + 1] & 0xFF);
    }

    private String getClassConstantName(int entryIndex) throws IOException {

        Constant entry = getConstantPoolEntry(entryIndex);
        if (entry == null) {
            return "";
        }
        return slashesToDots(toUTF8(entry.getNameIndex()));
    }

    private String toUTF8(int entryIndex) throws IOException {
        Constant entry = getConstantPoolEntry(entryIndex);
        if (entry.getTag() == CONSTANT_UTF8) {
            return (String) entry.getValue();
        }

        throw new IOException("Constant pool entry is not a UTF8 type: " + entryIndex);
    }

    private void addImport(String importPackage) {
        if (importPackage != null && collector.accept(importPackage)) {
            jClass.addImport(new JavaPackage(importPackage));
        }
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    private String getPackageName(String s) {
        if ((s.length() > 0) && (s.charAt(0) == '[')) {
            String types[] = descriptorToTypes(s);
            if (types.length == 0) {
                return null; // primitives
            }

            s = types[0];
        }

        s = slashesToDots(s);
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(0, index);
        }

        return "Default";
    }

    private String[] descriptorToTypes(String descriptor) {

        int typesCount = 0;
        for (int index = 0; index < descriptor.length(); index++) {
            if (descriptor.charAt(index) == ';') {
                typesCount++;
            }
        }

        String types[] = new String[typesCount];

        int typeIndex = 0;
        for (int index = 0; index < descriptor.length(); index++) {

            int startIndex = descriptor.indexOf(CLASS_DESCRIPTOR, index);
            if (startIndex < 0) {
                break;
            }

            index = descriptor.indexOf(';', startIndex + 1);
            types[typeIndex++] = descriptor.substring(startIndex + 1, index);
        }

        return types;
    }

    class Constant {

        private final byte _tag;

        private final int _nameIndex;

        private final int _typeIndex;

        private Object _value;

        Constant(byte tag, int nameIndex) {
            this(tag, nameIndex, -1);
        }

        Constant(byte tag, Object value) {
            this(tag, -1, -1);
            _value = value;
        }

        Constant(byte tag, int nameIndex, int typeIndex) {
            _tag = tag;
            _nameIndex = nameIndex;
            _typeIndex = typeIndex;
            _value = null;
        }

        byte getTag() {
            return _tag;
        }

        int getNameIndex() {
            return _nameIndex;
        }

        int getTypeIndex() {
            return _typeIndex;
        }

        Object getValue() {
            return _value;
        }

        @Override
        public String toString() {

            StringBuilder s = new StringBuilder("");

            s.append("tag: " + getTag());

            if (getNameIndex() > -1) {
                s.append(" nameIndex: " + getNameIndex());
            }

            if (getTypeIndex() > -1) {
                s.append(" typeIndex: " + getTypeIndex());
            }

            if (getValue() != null) {
                s.append(" value: " + getValue());
            }

            return s.toString();
        }
    }

    class FieldOrMethodInfo {

        private final int _accessFlags;

        private final int _nameIndex;

        private final int _descriptorIndex;

        private AttributeInfo _runtimeVisibleAnnotations;
        private AttributeInfo _signature;

        FieldOrMethodInfo(int accessFlags, int nameIndex, int descriptorIndex) {
            _accessFlags = accessFlags;
            _nameIndex = nameIndex;
            _descriptorIndex = descriptorIndex;
        }

        int accessFlags() {
            return _accessFlags;
        }

        int getNameIndex() {
            return _nameIndex;
        }

        int getDescriptorIndex() {
            return _descriptorIndex;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("");

            try {
                s.append("\n    name (#" + getNameIndex() + ") = " + toUTF8(getNameIndex()));
                s.append("\n    signature (#" + getDescriptorIndex() + ") = " + toUTF8(getDescriptorIndex()));

                String[] types = descriptorToTypes(toUTF8(getDescriptorIndex()));
                for (String type : types) {
                    s.append("\n        type = " + type);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return s.toString();
        }
    }

    class AttributeInfo {

        private String name;

        private byte[] value;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }

        public byte[] getValue() {
            return this.value;
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return String representation.
     */
    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();

        try {

            s.append("\n" + className + ":\n");

            s.append("\nConstants:\n");
            for (int i = 1; i < constantPool.length; i++) {
                Constant entry = getConstantPoolEntry(i);
                s.append("    " + i + ". " + entry.toString() + "\n");
                if (entry.getTag() == CONSTANT_DOUBLE || entry.getTag() == CONSTANT_LONG) {
                    i++;
                }
            }

            s.append("\nClass Name: " + className + "\n");
            s.append("Super Name: " + superClassName + "\n\n");

            s.append(interfaceNames.length + " interfaces\n");
            for (String interfaceName : interfaceNames) {
                s.append("    " + interfaceName + "\n");
            }

            s.append("\n" + fields.length + " fields\n");
            for (FieldOrMethodInfo field : fields) {
                s.append(field.toString() + "\n");
            }

            s.append("\n" + methods.length + " methods\n");
            for (FieldOrMethodInfo method : methods) {
                s.append(method.toString() + "\n");
            }

            s.append("\nDependencies:\n");
            for (JavaPackage jPackage : jClass.getImports()) {
                s.append("    " + jPackage.getName() + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return s.toString();
    }
}
