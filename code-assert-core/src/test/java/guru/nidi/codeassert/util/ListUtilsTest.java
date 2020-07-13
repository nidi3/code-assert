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
package guru.nidi.codeassert.util;

import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.util.ListUtils.andJoin;
import static guru.nidi.codeassert.util.ListUtils.join;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListUtilsTest {

    @Test
    void emptyArray() {
        assertEquals("", join(",", new String[]{}));
    }

    @Test
    void oneElementArray() {
        assertEquals("a", join(",", new String[]{"a"}));
    }

    @Test
    void multiElementArray() {
        assertEquals("a,b", join(",", new String[]{"a", "b"}));
    }

    @Test
    void notStringArray() {
        assertEquals("1,2", join(",", new Integer[]{1, 2}));
    }

    @Test
    void emptyList() {
        assertEquals("", join(",", asList()));
    }

    @Test
    void oneElementList() {
        assertEquals("a", join(",", asList("a")));
    }

    @Test
    void multiElementList() {
        assertEquals("a,b", join(",", asList("a", "b")));
    }

    @Test
    void notStringList() {
        assertEquals("1,2", join(",", asList(1, 2)));
    }

    @Test
    void andJoinArray() {
        assertEquals(String.format("start%n1%n2"), andJoin("start", new Integer[]{1, 2}));
    }

    @Test
    void andJoinList() {
        assertEquals(String.format("start%n1%n2"), andJoin("start", asList(1, 2)));
    }
}
