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

final class AttributeInfo {
    final String name;
    final byte[] value;

    private AttributeInfo(String name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    static AttributeInfo fromData(DataInputStream in, ConstantPool constantPool) throws IOException {
        final String name = constantPool.getUtf8(in.readUnsignedShort());
        final int attributeLength = in.readInt();
        final byte[] value = new byte[attributeLength];
        for (int b = 0; b < attributeLength; b++) {
            value[b] = in.readByte();
        }

        return new AttributeInfo(name, value);
    }

    boolean isAnnotation() {
        return "RuntimeVisibleAnnotations".equals(name) || "RuntimeInvisibleAnnotations".equals(name);
    }

    boolean isSignature() {
        return "Signature".equals(name);
    }

    boolean isSource() {
        return "SourceFile".equals(name);
    }

    boolean isCode() {
        return "Code".equals(name);
    }

    String sourceFile(ConstantPool constantPool) throws IOException {
        final int b0 = value[0] < 0 ? value[0] + 256 : value[0];
        final int b1 = value[1] < 0 ? value[1] + 256 : value[1];
        final int pe = b0 * 256 + b1;
        return constantPool.getUtf8(pe);
    }

    int u2(int index) {
        return (value[index] << 8 & 0xFF00) | (value[index + 1] & 0xFF);
    }
}
