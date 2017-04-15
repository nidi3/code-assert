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
import java.util.Iterator;
import java.util.NoSuchElementException;

final class ConstantPool implements Iterable<Constant> {
    private final Constant[] pool;

    private ConstantPool(Constant[] pool) {
        this.pool = pool;
    }

    public static ConstantPool fromData(DataInputStream in) throws IOException {
        final int size = in.readUnsignedShort();
        final Constant[] pool = new Constant[size];
        for (int i = 1; i < size; i++) {
            final Constant constant = Constant.fromData(in);
            pool[i] = constant;

            // 8-byte constants use two constant pool entries
            if (constant.isBig()) {
                i++;
            }
        }
        return new ConstantPool(pool);
    }

    public Constant getEntry(int entryIndex) throws IOException {
        if (entryIndex < 0 || entryIndex >= pool.length) {
            throw new IOException("Illegal constant pool index : " + entryIndex);
        }

        return pool[entryIndex];
    }

    public String getClassConstantName(int entryIndex) throws IOException {
        final Constant entry = getEntry(entryIndex);
        if (entry == null) {
            return "";
        }
        return slashesToDots(getUtf8(entry.nameIndex));
    }

    private String slashesToDots(String s) {
        return s.replace('/', '.');
    }

    public String getUtf8(int entryIndex) throws IOException {
        final Constant entry = getEntry(entryIndex);
        if (entry.tag == Constant.UTF8) {
            return (String) entry.value;
        }
        throw new IOException("Constant pool entry is not a UTF8 type: " + entryIndex);
    }

    public Iterator<Constant> iterator() {
        return new Iterator<Constant>() {
            int pos = 1;

            @Override
            public boolean hasNext() {
                return pos < pool.length;
            }

            @Override
            public Constant next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final Constant constant = pool[pos];
                pos += constant.isBig() ? 2 : 1;
                return constant;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

