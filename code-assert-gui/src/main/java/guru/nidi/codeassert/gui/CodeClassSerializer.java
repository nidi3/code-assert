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
import guru.nidi.codeassert.model.CodeClass;
import guru.nidi.codeassert.model.CodePackage;

import java.io.IOException;
import java.util.Map;

public class CodeClassSerializer extends StdSerializer<CodeClass> {
    protected CodeClassSerializer(Class<CodeClass> t) {
        super(t);
    }

    @Override
    public void serialize(CodeClass value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeStringField("package", value.getPackage().getName());
        gen.writeNumberField("size", value.getTotalSize());
        gen.writeArrayFieldStart("usePackages");
        for (final CodePackage pack : value.usedPackages()) {
            gen.writeString(pack.getName());
        }
        gen.writeEndArray();
        gen.writeObjectFieldStart("useClasses");
        for (final Map.Entry<CodeClass, Integer> entry : value.usedClassCounts().entrySet()) {
            gen.writeNumberField(entry.getKey().getName(), entry.getValue());
        }
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
