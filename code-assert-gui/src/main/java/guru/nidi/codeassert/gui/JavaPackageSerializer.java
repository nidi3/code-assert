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
package guru.nidi.codeassert.gui;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import guru.nidi.codeassert.model.JavaClass;
import guru.nidi.codeassert.model.JavaPackage;

import java.io.IOException;

public class JavaPackageSerializer extends StdSerializer<JavaPackage> {
    protected JavaPackageSerializer(Class<JavaPackage> t) {
        super(t);
    }

    @Override
    public void serialize(JavaPackage value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeArrayFieldStart("classes");
        for (final JavaClass clazz : value.getClasses()) {
            gen.writeString(clazz.getName());
        }
        gen.writeEndArray();
        gen.writeArrayFieldStart("uses");
        for (final JavaPackage pack : value.uses()) {
            gen.writeString(pack.getName());
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

}
