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

import guru.nidi.codeassert.PackageCollector;

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
    static final int CONSTANT_UTF8 = 1;
    private static final int CONSTANT_UNICODE = 2;
    private static final int CONSTANT_INTEGER = 3;
    private static final int CONSTANT_FLOAT = 4;
    static final int CONSTANT_LONG = 5;
    static final int CONSTANT_DOUBLE = 6;
    static final int CONSTANT_CLASS = 7;
    private static final int CONSTANT_STRING = 8;
    private static final int CONSTANT_FIELD = 9;
    private static final int CONSTANT_METHOD = 10;
    private static final int CONSTANT_INTERFACEMETHOD = 11;
    private static final int CONSTANT_NAMEANDTYPE = 12;

    private static final int CONSTANT_METHOD_HANDLE = 15;
    private static final int CONSTANT_METHOD_TYPE = 16;
    private static final int CONSTANT_INVOKEDYNAMIC = 18;

    static final char CLASS_DESCRIPTOR = 'L';
    private static final int ACC_INTERFACE = 0x200;
    private static final int ACC_ABSTRACT = 0x400;

    private static final String ATTR_ANNOTATIONS = "RuntimeVisibleAnnotations";
    private static final String ATTR_SIGNATURE = "Signature";
    private static final String ATTR_SOURCE = "SourceFile";

    private final PackageCollector collector;
    private JavaClass jClass;
    private Constant[] constantPool;
    private DataInputStream in;


    public ClassFileParser() {
        this(PackageCollector.all());
    }

    public ClassFileParser(PackageCollector collector) {
        this.collector = collector;
    }

    public JavaClass parse(File file) throws IOException {
        try (final InputStream in = new FileInputStream(file)) {
            return parse(in);
        }
    }

    public JavaClass parse(InputStream is) throws IOException {
        jClass = new JavaClass("Unknown");
        in = new DataInputStream(is);

        parseMagic();
        parseMinorVersion();
        parseMajorVersion();

        constantPool = parseConstantPool();

        parseAccessFlags();

        String className = parseClassName();
        String superClassName = parseSuperClassName();
        String[] interfaceNames = parseInterfaces();
        FieldOrMethodInfo[] fields = parseFields();
        FieldOrMethodInfo[] methods = parseMethods();
        AttributeInfo[] attributes = parseAttributes();

        final JavaClassImportBuilder adder = new JavaClassImportBuilder(jClass, collector, constantPool);
        adder.addClassName(className);
        adder.addClassConstantReferences();
        adder.addSuperClass(superClassName);
        adder.addInterfaces(interfaceNames);
        adder.addFieldRefs(fields);
        adder.addMethodRefs(methods);
        adder.addAttributeRefs(attributes);

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
            if (constant.tag == CONSTANT_DOUBLE || constant.tag == CONSTANT_LONG) {
                i++;
            }
        }

        return pool;
    }

    private void parseAccessFlags() throws IOException {
        in.readUnsignedShort();
    }

    private String parseClassName() throws IOException {
        int entryIndex = in.readUnsignedShort();
        String className = getClassConstantName(entryIndex);
        jClass.setName(className);

        return className;
    }

    private String parseSuperClassName() throws IOException {
        int entryIndex = in.readUnsignedShort();
        return getClassConstantName(entryIndex);
    }

    private String[] parseInterfaces() throws IOException {
        int interfacesCount = in.readUnsignedShort();
        String[] interfaceNames = new String[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            int entryIndex = in.readUnsignedShort();
            interfaceNames[i] = getClassConstantName(entryIndex);
        }
        return interfaceNames;
    }

    private FieldOrMethodInfo[] parseFields() throws IOException {
        int fieldsCount = in.readUnsignedShort();
        FieldOrMethodInfo[] fields = new FieldOrMethodInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            fields[i] = parseFieldOrMethodInfo();
        }

        return fields;
    }

    private FieldOrMethodInfo[] parseMethods() throws IOException {
        int methodsCount = in.readUnsignedShort();
        FieldOrMethodInfo[] methods = new FieldOrMethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            methods[i] = parseFieldOrMethodInfo();
        }

        return methods;
    }

    private Constant parseNextConstant() throws IOException {
        byte tag = in.readByte();

        switch (tag) {
            case CONSTANT_CLASS:
            case CONSTANT_STRING:
            case CONSTANT_METHOD_TYPE:
                return new Constant(tag, in.readUnsignedShort());
            case CONSTANT_FIELD:
            case CONSTANT_METHOD:
            case CONSTANT_INTERFACEMETHOD:
            case CONSTANT_NAMEANDTYPE:
            case CONSTANT_INVOKEDYNAMIC:
                return new Constant(tag, in.readUnsignedShort(), in.readUnsignedShort());
            case CONSTANT_INTEGER:
                return new Constant(tag, in.readInt());
            case CONSTANT_FLOAT:
                return new Constant(tag, in.readFloat());
            case CONSTANT_LONG:
                return new Constant(tag, in.readLong());
            case CONSTANT_DOUBLE:
                return new Constant(tag, in.readDouble());
            case CONSTANT_UTF8:
                return new Constant(tag, in.readUTF());
            case CONSTANT_METHOD_HANDLE:
                return new Constant(tag, in.readByte(), in.readUnsignedShort());
            default:
                throw new IOException("Unknown constant: " + tag);
        }
    }

    private FieldOrMethodInfo parseFieldOrMethodInfo() throws IOException {
        FieldOrMethodInfo result = new FieldOrMethodInfo(in.readUnsignedShort(), in.readUnsignedShort(), in.readUnsignedShort());

        int attributesCount = in.readUnsignedShort();
        for (int a = 0; a < attributesCount; a++) {
            AttributeInfo attribute = parseAttribute();
            if (ATTR_ANNOTATIONS.equals(attribute.name)) {
                result.runtimeVisibleAnnotations = attribute;
            }
            if (ATTR_SIGNATURE.equals(attribute.name)) {
                result.signature = attribute;
            }
        }

        return result;
    }

    private AttributeInfo[] parseAttributes() throws IOException {
        int attributesCount = in.readUnsignedShort();
        AttributeInfo[] attributes = new AttributeInfo[attributesCount];

        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = parseAttribute();

            // Section 4.7.7 of VM Spec - Class File Format
            if (ATTR_SOURCE.equals(attributes[i].name)) {
                byte[] b = attributes[i].value;
                int b0 = b[0] < 0 ? b[0] + 256 : b[0];
                int b1 = b[1] < 0 ? b[1] + 256 : b[1];
                int pe = b0 * 256 + b1;

                String descriptor = toUTF8(pe);
                jClass.setSourceFile(descriptor);
            }
        }
        return attributes;
    }

    private AttributeInfo parseAttribute() throws IOException {
        AttributeInfo result = new AttributeInfo();

        int nameIndex = in.readUnsignedShort();
        if (nameIndex != -1) {
            result.name = toUTF8(nameIndex);
        }

        int attributeLength = in.readInt();
        byte[] value = new byte[attributeLength];
        for (int b = 0; b < attributeLength; b++) {
            value[b] = in.readByte();
        }

        result.value = value;
        return result;
    }

    private Constant getConstantPoolEntry(int entryIndex) throws IOException {
        if (entryIndex < 0 || entryIndex >= constantPool.length) {
            throw new IOException("Illegal constant pool index : " + entryIndex);
        }

        return constantPool[entryIndex];
    }

    private String getClassConstantName(int entryIndex) throws IOException {
        Constant entry = getConstantPoolEntry(entryIndex);
        if (entry == null) {
            return "";
        }
        return slashesToDots(toUTF8(entry.nameIndex));
    }

    private String toUTF8(int entryIndex) throws IOException {
        Constant entry = getConstantPoolEntry(entryIndex);
        if (entry.tag == CONSTANT_UTF8) {
            return (String) entry.value;
        }

        throw new IOException("Constant pool entry is not a UTF8 type: " + entryIndex);
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    static class Constant {
        final byte tag;
        final int nameIndex;
        final int typeIndex;
        final Object value;

        Constant(byte tag, int nameIndex) {
            this(tag, nameIndex, -1, null);
        }

        Constant(byte tag, Object value) {
            this(tag, -1, -1, value);
        }

        Constant(byte tag, int nameIndex, int typeIndex) {
            this(tag, nameIndex, typeIndex, null);
        }

        Constant(byte tag, int nameIndex, int typeIndex, Object value) {
            this.tag = tag;
            this.nameIndex = nameIndex;
            this.typeIndex = typeIndex;
            this.value = value;
        }
    }

    static class FieldOrMethodInfo {
        final int accessFlags;
        final int nameIndex;
        final int descriptorIndex;

        AttributeInfo runtimeVisibleAnnotations;
        AttributeInfo signature;

        FieldOrMethodInfo(int accessFlags, int nameIndex, int descriptorIndex) {
            this.accessFlags = accessFlags;
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }
    }

    static class AttributeInfo {
        String name;
        byte[] value;
    }

}
