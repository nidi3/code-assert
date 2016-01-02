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

        final String className = parseClassName();
        final String superClassName = parseSuperClassName();
        final String[] interfaceNames = parseInterfaces();
        final FieldOrMethodInfo[] fields = parseFields();
        final FieldOrMethodInfo[] methods = parseMethods();
        final AttributeInfo[] attributes = parseAttributes();

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
        final int magic = in.readInt();
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
        final int constantPoolSize = in.readUnsignedShort();
        final Constant[] pool = new Constant[constantPoolSize];
        for (int i = 1; i < constantPoolSize; i++) {
            final Constant constant = Constant.fromData(in);
            pool[i] = constant;

            // 8-byte constants use two constant pool entries
            if (constant.isBig()) {
                i++;
            }
        }

        return pool;
    }

    private void parseAccessFlags() throws IOException {
        in.readUnsignedShort();
    }

    private String parseClassName() throws IOException {
        final int entryIndex = in.readUnsignedShort();
        final String className = getClassConstantName(entryIndex);
        jClass.setName(className);

        return className;
    }

    private String parseSuperClassName() throws IOException {
        final int entryIndex = in.readUnsignedShort();
        return getClassConstantName(entryIndex);
    }

    private String[] parseInterfaces() throws IOException {
        final int interfacesCount = in.readUnsignedShort();
        final String[] interfaceNames = new String[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            final int entryIndex = in.readUnsignedShort();
            interfaceNames[i] = getClassConstantName(entryIndex);
        }
        return interfaceNames;
    }

    private FieldOrMethodInfo[] parseFields() throws IOException {
        final int fieldsCount = in.readUnsignedShort();
        final FieldOrMethodInfo[] fields = new FieldOrMethodInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            fields[i] = parseFieldOrMethodInfo();
        }

        return fields;
    }

    private FieldOrMethodInfo[] parseMethods() throws IOException {
        final int methodsCount = in.readUnsignedShort();
        final FieldOrMethodInfo[] methods = new FieldOrMethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            methods[i] = parseFieldOrMethodInfo();
        }

        return methods;
    }

    private FieldOrMethodInfo parseFieldOrMethodInfo() throws IOException {
        final FieldOrMethodInfo result = new FieldOrMethodInfo(in.readUnsignedShort(), in.readUnsignedShort(), in.readUnsignedShort());
        final int attributesCount = in.readUnsignedShort();
        for (int a = 0; a < attributesCount; a++) {
            final AttributeInfo attribute = parseAttribute();
            if (attribute.isAnnotation()) {
                result.runtimeVisibleAnnotations = attribute;
            }
            if (attribute.isSignature()) {
                result.signature = attribute;
            }
        }

        return result;
    }

    private AttributeInfo[] parseAttributes() throws IOException {
        final int attributesCount = in.readUnsignedShort();
        final AttributeInfo[] attributes = new AttributeInfo[attributesCount];
        for (int i = 0; i < attributesCount; i++) {
            attributes[i] = parseAttribute();

            // Section 4.7.7 of VM Spec - Class File Format
            if (attributes[i].isSource()) {
                final byte[] b = attributes[i].value;
                final int b0 = b[0] < 0 ? b[0] + 256 : b[0];
                final int b1 = b[1] < 0 ? b[1] + 256 : b[1];
                final int pe = b0 * 256 + b1;
                jClass.setSourceFile(toUTF8(pe));
            }
        }
        return attributes;
    }

    private AttributeInfo parseAttribute() throws IOException {
        final AttributeInfo result = new AttributeInfo();

        final int nameIndex = in.readUnsignedShort();
        if (nameIndex != -1) {
            result.name = toUTF8(nameIndex);
        }

        final int attributeLength = in.readInt();
        final byte[] value = new byte[attributeLength];
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
        final Constant entry = getConstantPoolEntry(entryIndex);
        if (entry == null) {
            return "";
        }
        return slashesToDots(toUTF8(entry.nameIndex));
    }

    private String toUTF8(int entryIndex) throws IOException {
        final Constant entry = getConstantPoolEntry(entryIndex);
        if (entry.tag == Constant.UTF8) {
            return (String) entry.value;
        }

        throw new IOException("Constant pool entry is not a UTF8 type: " + entryIndex);
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    static class Constant {
        public static final int
                UTF8 = 1,
                UNICODE = 2,
                INTEGER = 3,
                FLOAT = 4,
                LONG = 5,
                DOUBLE = 6,
                CLASS = 7,
                STRING = 8,
                FIELD = 9,
                METHOD = 10,
                INTERFACEMETHOD = 11,
                NAMEANDTYPE = 12,
                METHOD_HANDLE = 15,
                METHOD_TYPE = 16,
                INVOKEDYNAMIC = 18;

        final byte tag;
        final int nameIndex;
        final int typeIndex;
        final Object value;

        public static Constant fromData(DataInputStream in) throws IOException {
            final byte tag = in.readByte();
            switch (tag) {
                case Constant.CLASS:
                case Constant.STRING:
                case Constant.METHOD_TYPE:
                    return new Constant(tag, in.readUnsignedShort());
                case Constant.FIELD:
                case Constant.METHOD:
                case Constant.INTERFACEMETHOD:
                case Constant.NAMEANDTYPE:
                case Constant.INVOKEDYNAMIC:
                    return new Constant(tag, in.readUnsignedShort(), in.readUnsignedShort());
                case Constant.INTEGER:
                    return new Constant(tag, in.readInt());
                case Constant.FLOAT:
                    return new Constant(tag, in.readFloat());
                case Constant.LONG:
                    return new Constant(tag, in.readLong());
                case Constant.DOUBLE:
                    return new Constant(tag, in.readDouble());
                case Constant.UTF8:
                    return new Constant(tag, in.readUTF());
                case Constant.METHOD_HANDLE:
                    return new Constant(tag, in.readByte(), in.readUnsignedShort());
                default:
                    throw new IOException("Unknown constant: " + tag);
            }
        }

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

        public boolean isBig() {
            return tag == DOUBLE || tag == LONG;
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

        public boolean isAnnotation() {
            return "RuntimeVisibleAnnotations".equals(name);
        }

        public boolean isSignature() {
            return "Signature".equals(name);
        }

        public boolean isSource() {
            return "SourceFile".equals(name);
        }
    }

}
