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

final class Constant {
    static final int
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

    static Constant fromData(DataInputStream in) throws IOException {
        final byte tag = in.readByte();
        switch (tag) {
            case CLASS:
            case STRING:
            case METHOD_TYPE:
                return new Constant(tag, in.readUnsignedShort());
            case FIELD:
            case METHOD:
            case INTERFACEMETHOD:
            case NAMEANDTYPE:
            case INVOKEDYNAMIC:
                return new Constant(tag, in.readUnsignedShort(), in.readUnsignedShort());
            case INTEGER:
                return new Constant(tag, in.readInt());
            case FLOAT:
                return new Constant(tag, in.readFloat());
            case LONG:
                return new Constant(tag, in.readLong());
            case DOUBLE:
                return new Constant(tag, in.readDouble());
            case UTF8:
                return new Constant(tag, in.readUTF());
            case METHOD_HANDLE:
                return new Constant(tag, in.readByte(), in.readUnsignedShort());
            default:
                throw new IOException("Unknown constant: " + tag);
        }
    }

    private Constant(byte tag, int nameIndex) {
        this(tag, nameIndex, -1, null);
    }

    private Constant(byte tag, Object value) {
        this(tag, -1, -1, value);
    }

    private Constant(byte tag, int nameIndex, int typeIndex) {
        this(tag, nameIndex, typeIndex, null);
    }

    private Constant(byte tag, int nameIndex, int typeIndex, Object value) {
        this.tag = tag;
        this.nameIndex = nameIndex;
        this.typeIndex = typeIndex;
        this.value = value;
    }

    boolean isBig() {
        return tag == DOUBLE || tag == LONG;
    }
}
