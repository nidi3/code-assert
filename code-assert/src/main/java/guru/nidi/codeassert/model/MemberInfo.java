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

import java.io.DataInputStream;
import java.io.IOException;

public final class MemberInfo {
    private final int accessFlags;
    private final String name;
    final String descriptor;
    final AttributeInfo annotations;
    final AttributeInfo signature;
    final int codeSize;

    private MemberInfo(int accessFlags, String name, String descriptor,
                       AttributeInfo annotations, AttributeInfo signature, int codeSize) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.annotations = annotations;
        this.signature = signature;
        this.codeSize = codeSize;
    }

    static MemberInfo fromData(DataInputStream in, ConstantPool constantPool) throws IOException {
        final int access = in.readUnsignedShort();
        final String name = constantPool.getUtf8(in.readUnsignedShort());
        final String descriptor = constantPool.getUtf8(in.readUnsignedShort());
        final int attributesCount = in.readUnsignedShort();
        AttributeInfo annotations = null;
        AttributeInfo signature = null;
        int codeSize = 0;
        for (int a = 0; a < attributesCount; a++) {
            final AttributeInfo attribute = AttributeInfo.fromData(in, constantPool);
            if (attribute.isAnnotation()) {
                annotations = attribute;
            }
            if (attribute.isSignature()) {
                signature = attribute;
            }
            if (attribute.isCode()) {
                codeSize = attribute.value.length;
            }
        }
        return new MemberInfo(access, name, descriptor, annotations, signature, codeSize);
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public String getName() {
        return name;
    }

    public int getCodeSize() {
        return codeSize;
    }
}
