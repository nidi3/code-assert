/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ListUtilsTest {

    @Test
    public void emptyArray() {
        assertEquals("", ListUtils.join(",", new String[]{}));
    }

    @Test
    public void oneElementArray() {
        assertEquals("a", ListUtils.join(",", new String[]{"a"}));
    }

    @Test
    public void multiElementArray() {
        assertEquals("a,b", ListUtils.join(",", new String[]{"a", "b"}));
    }

    @Test
    public void notStringArray() {
        assertEquals("1,2", ListUtils.join(",", new Integer[]{1, 2}));
    }

    @Test
    public void emptyList() {
        assertEquals("", ListUtils.join(",", Arrays.asList()));
    }

    @Test
    public void oneElementList() {
        assertEquals("a", ListUtils.join(",", Arrays.asList("a")));
    }

    @Test
    public void multiElementList() {
        assertEquals("a,b", ListUtils.join(",", Arrays.asList("a", "b")));
    }

    @Test
    public void notStringList() {
        assertEquals("1,2", ListUtils.join(",", Arrays.asList(1, 2)));
    }
}