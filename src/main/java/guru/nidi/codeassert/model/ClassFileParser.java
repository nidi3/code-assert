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

    private JavaClass jClass;
    private ConstantPool constantPool;
    private DataInputStream in;

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

        constantPool = ConstantPool.fromData(in);

        parseAccessFlags();

        final String className = parseClassName();
        final String superClassName = parseSuperClassName();
        final String[] interfaceNames = parseInterfaces();
        final MemberInfo[] fields = parseMembers();
        final MemberInfo[] methods = parseMembers();
        final AttributeInfo[] attributes = parseAttributes();

        final JavaClassImportBuilder adder = new JavaClassImportBuilder(jClass, constantPool);
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

    private void parseAccessFlags() throws IOException {
        in.readUnsignedShort();
    }

    private String parseClassName() throws IOException {
        final int entryIndex = in.readUnsignedShort();
        final String className = constantPool.getClassConstantName(entryIndex);
        jClass.setName(className);

        return className;
    }

    private String parseSuperClassName() throws IOException {
        final int entryIndex = in.readUnsignedShort();
        return constantPool.getClassConstantName(entryIndex);
    }

    private String[] parseInterfaces() throws IOException {
        final int count = in.readUnsignedShort();
        final String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            final int entryIndex = in.readUnsignedShort();
            names[i] = constantPool.getClassConstantName(entryIndex);
        }
        return names;
    }

    private MemberInfo[] parseMembers() throws IOException {
        final int count = in.readUnsignedShort();
        final MemberInfo[] infos = new MemberInfo[count];
        for (int i = 0; i < count; i++) {
            infos[i] = MemberInfo.fromData(in, constantPool);
        }
        return infos;
    }

    private AttributeInfo[] parseAttributes() throws IOException {
        final int count = in.readUnsignedShort();
        final AttributeInfo[] attributes = new AttributeInfo[count];
        for (int i = 0; i < count; i++) {
            attributes[i] = AttributeInfo.fromData(in, constantPool);

            // Section 4.7.7 of VM Spec - Class File Format
            if (attributes[i].isSource()) {
                jClass.setSourceFile(attributes[i].sourceFile(constantPool));
            }
        }
        return attributes;
    }

}
