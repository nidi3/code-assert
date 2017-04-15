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

final class MemberInfo {
    final int accessFlags;
    final int nameIndex;
    final int descriptorIndex;
    final AttributeInfo annotations;
    final AttributeInfo signature;

    private MemberInfo(int accessFlags, int nameIndex, int descriptorIndex,
                       AttributeInfo annotations, AttributeInfo signature) {
        this.accessFlags = accessFlags;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.annotations = annotations;
        this.signature = signature;
    }

    public static MemberInfo fromData(DataInputStream in, ConstantPool constantPool) throws IOException {
        final int access = in.readUnsignedShort();
        final int nameIndex = in.readUnsignedShort();
        final int descriptorIndex = in.readUnsignedShort();
        final int attributesCount = in.readUnsignedShort();
        AttributeInfo annotations = null;
        AttributeInfo signature = null;
        for (int a = 0; a < attributesCount; a++) {
            final AttributeInfo attribute = AttributeInfo.fromData(in, constantPool);
            if (attribute.isAnnotation()) {
                annotations = attribute;
            }
            if (attribute.isSignature()) {
                signature = attribute;
            }
        }
        return new MemberInfo(access, nameIndex, descriptorIndex, annotations, signature);
    }
}
