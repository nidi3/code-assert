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

import org.apache.commons.io.input.CountingInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    private ConstantPool constantPool;
    private DataInputStream in;

    CodeClass parse(File file, Model model) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return parse(in, model);
        }
    }

    CodeClass parse(InputStream is, Model model) throws IOException {
        final CountingInputStream counter = new CountingInputStream(is);
        in = new DataInputStream(counter);

        parseMagic();
        parseMinorVersion();
        parseMajorVersion();

        constantPool = ConstantPool.fromData(in);

        final int flags = parseAccessFlags();

        final String className = parseClassName();
        final String superClassName = parseSuperClassName();
        final List<String> interfaceNames = parseInterfaces();
        final List<MemberInfo> fields = parseMembers();
        final List<MemberInfo> methods = parseMembers();
        final List<AttributeInfo> attributes = parseAttributes();

        return new CodeClassBuilder(className, model, constantPool)
                .addClassConstantReferences()
                .addFlags(flags)
                .addSuperClass(superClassName)
                .addInterfaces(interfaceNames)
                .addFieldRefs(fields)
                .addMethodRefs(methods)
                .addAttributeRefs(attributes)
                .addPackageInfo(model, className)
                .addCodeSizes(counter.getCount(), methods)
                .clazz;
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

    private int parseAccessFlags() throws IOException {
        return in.readUnsignedShort();
    }

    private String parseClassName() throws IOException {
        final int entryIndex = in.readUnsignedShort();
        return constantPool.getClassConstantName(entryIndex);
    }

    private String parseSuperClassName() throws IOException {
        final int entryIndex = in.readUnsignedShort();
        return constantPool.getClassConstantName(entryIndex);
    }

    private List<String> parseInterfaces() throws IOException {
        final int count = in.readUnsignedShort();
        final List<String> names = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final int entryIndex = in.readUnsignedShort();
            names.add(constantPool.getClassConstantName(entryIndex));
        }
        return names;
    }

    private List<MemberInfo> parseMembers() throws IOException {
        final int count = in.readUnsignedShort();
        final List<MemberInfo> infos = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            infos.add(MemberInfo.fromData(in, constantPool));
        }
        return infos;
    }

    private List<AttributeInfo> parseAttributes() throws IOException {
        final int count = in.readUnsignedShort();
        final List<AttributeInfo> attributes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            attributes.add(AttributeInfo.fromData(in, constantPool));
        }
        return attributes;
    }

}
