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
package guru.nidi.codeassert.config;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static guru.nidi.codeassert.config.Language.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocationNameMatcherTest {
    @Test
    void basic() {
        final LocationNameMatcher m = new LocationNameMatcher(
                asList(Location.of("A"), Location.of("a.B"), Location.of("Y#c")),
                asList("c", "d"));
        assertTrue(m.matches("c", null, "A", null, true));
        assertTrue(m.matches("d", null, "a.B", null, true));
        assertTrue(m.matches("d", null, "Y", "c", true));
        assertTrue(m.matches("d", null, "A", "x", true));
        assertFalse(m.matches("e", null, "Y", "c", true));
        assertFalse(m.matches("d", null, "Y", "d", true));
    }

    @Test
    void emptyLocs() {
        final LocationNameMatcher m = new LocationNameMatcher(Collections.<Location>emptyList(), asList("c", "d"));
        assertTrue(m.matches("c", null, "X", null, true));
        assertTrue(m.matches("c", null, null, "#e", true));
        assertFalse(m.matches("e", null, "X", null, true));
    }

    @Test
    void emptyNames() {
        final LocationNameMatcher m = new LocationNameMatcher(
                asList(Location.of("A"), Location.of("a.B"), Location.of("x.Y#c")),
                Collections.<String>emptyList());
        assertTrue(m.matches("c", null, "A", null, true));
        assertFalse(m.matches("c", null, "B", null, true));
    }

    @Test
    void nonStrictNames() {
        final LocationNameMatcher m = new LocationNameMatcher(Collections.<Location>emptyList(), asList("cat"));
        assertTrue(m.matches("cat", null, null, null, false));
        assertTrue(m.matches("xcat", null, null, null, false));
        assertTrue(m.matches("catx", null, null, null, false));
        assertTrue(m.matches("xcatx", null, null, null, false));
        assertFalse(m.matches("ca", null, null, null, false));
    }

    @Test
    void language() {
        final LocationNameMatcher m = new LocationNameMatcher(
                asList(Location.of("java:A"), Location.of("kotlin:")), asList("c"));
        assertTrue(m.matches("c", null, "A", null, true));
        assertTrue(m.matches("c", JAVA, "A", null, true));
        assertTrue(m.matches("c", KOTLIN, "B", null, true));
        assertFalse(m.matches("c", SCALA, "A", null, true));
    }
}
