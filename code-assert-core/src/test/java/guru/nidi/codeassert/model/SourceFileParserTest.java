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

import guru.nidi.codeassert.config.Language;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static guru.nidi.codeassert.config.Language.JAVA;
import static guru.nidi.codeassert.config.Language.KOTLIN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceFileParserTest {
    @Test
    void simple() throws IOException {
        assertLines(3, 0, 1, 3, JAVA, "line\n  \nlast");
    }

    @Test
    void comment() throws IOException {
        assertLines(3, 4, 0, 6, JAVA, "line\n//line comment\ncode //comment\n/*\nmultiline comment*/\nlast");
    }

    @Test
    void nestedComment() throws IOException {
        assertLines(3, 2, 0, 4, JAVA, "line\n/*\nbla //comment */ code\nlast");
    }

    @Test
    void stringAndComment() throws IOException {
        assertLines(4, 0, 0, 4, JAVA, "line\n\" \\\"// no comment\"\n\"/*no comment \"\nlast");
        assertLines(3, 2, 0, 4, JAVA, "line\n/*\n\"*/ end of comment\nlast");
    }

    @Test
    void multilineStrings() throws IOException {
        assertLines(4, 3, 0, 6, JAVA, "line\n\"\"\"\"\n// line comment\n/*comment*/\n\"\"\"\" //line comment\nlast");
        assertLines(6, 1, 0, 6, KOTLIN, "line\n\"\"\"\"\n// line comment\n/*comment*/\n\"\"\" //line comment\nlast");
    }

    @Test
    void nestedComments() throws IOException {
        assertLines(3, 3, 0, 6, JAVA, "line\n/* level 1\n/* level 2\n*/\n*/\nlast");
        assertLines(2, 4, 0, 6, KOTLIN, "line\n/* level 1\n/* level 2\n*/\n*/\nlast");
    }

    private void assertLines(int code, int comment, int empty, int total, Language language, String source) throws IOException {
        final CodeClass clazz = new CodeClass("test", null);
        SourceFileParser.parse(clazz, language, new StringReader(source));
        assertEquals(code, clazz.codeLines);
        assertEquals(comment, clazz.commentLines);
        assertEquals(empty, clazz.emptyLines);
        assertEquals(total, clazz.totalLines);
    }

}
