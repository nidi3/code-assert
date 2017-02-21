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
package guru.nidi.codeassert.config;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationNameMatcherTest {
    @Test
    public void basic() {
        final LocationNameMatcher m = new LocationNameMatcher(Arrays.asList("A", "a.B", "Y#c"), Arrays.asList("c", "d"));
        assertTrue(m.matches("c", "A", null, true));
        assertTrue(m.matches("d", "a.B", null, true));
        assertTrue(m.matches("d", "Y", "c", true));
        assertTrue(m.matches("d", "A", "x", true));
        assertFalse(m.matches("e", "Y", "c", true));
        assertFalse(m.matches("d", "Y", "d", true));
    }

    @Test
    public void emptyLocs() {
        final LocationNameMatcher m = new LocationNameMatcher(Collections.<String>emptyList(), Arrays.asList("c", "d"));
        assertTrue(m.matches("c", "X", null, true));
        assertTrue(m.matches("c", null, "#e", true));
        assertFalse(m.matches("e", "X", null, true));
    }

    @Test
    public void emptyNames() {
        final LocationNameMatcher m = new LocationNameMatcher(Arrays.asList("A", "a.B", "x.Y#c"), Collections.<String>emptyList());
        assertTrue(m.matches("c", "A", null, true));
        assertFalse(m.matches("c", "B", null, true));
    }

    @Test
    public void nonStrictNames() {
        final LocationNameMatcher m = new LocationNameMatcher(Collections.<String>emptyList(), Arrays.asList("cat"));
        assertTrue(m.matches("cat", null, null, false));
        assertTrue(m.matches("xcat", null, null, false));
        assertTrue(m.matches("catx", null, null, false));
        assertTrue(m.matches("xcatx", null, null, false));
        assertFalse(m.matches("ca", null, null, false));
    }
}