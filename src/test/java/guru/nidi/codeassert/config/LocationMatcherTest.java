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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class LocationMatcherTest {
    @Test(expected = IllegalArgumentException.class)
    public void emptyPattern() {
        new LocationMatcher("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPattern() {
        new LocationMatcher(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wildcardInMiddle() {
        new LocationMatcher("a*b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void doubleWildcard() {
        new LocationMatcher("**");
    }

    @Test(expected = IllegalArgumentException.class)
    public void wildcardBetweenPackageAndClass() {
        new LocationMatcher("a*B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalWildcardInPackage3() {
        new LocationMatcher("*a*b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalWildcardInClass() {
        new LocationMatcher("A*b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalWildcardInMethod() {
        new LocationMatcher("#a*b");
    }

    @Test
    public void all() {
        final LocationMatcher m = new LocationMatcher("*");
        assertTrue(m.matches("pa"));
        assertTrue(m.matches("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
    }

    @Test
    public void methodOnly() {
        final LocationMatcher m = new LocationMatcher("#me");
        assertFalse(m.matches("pa"));
        assertFalse(m.matches("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matches("pa", "Cl", "mu"));
    }

    @Test
    public void classOnlyUppercase() {
        final LocationMatcher m = new LocationMatcher("Cl");
        assertFalse(m.matches("pa"));
        assertTrue(m.matches("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matches("pa", "Co"));
    }

    @Test
    public void classOnlySlash() {
        final LocationMatcher m = new LocationMatcher("/Cl");
        assertFalse(m.matches("pa"));
        assertTrue(m.matches("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matches("pa", "Co"));
    }

    @Test
    public void packageOnly() {
        final LocationMatcher m = new LocationMatcher("pa");
        assertTrue(m.matches("pa"));
        assertTrue(m.matches("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matches("po"));
    }

    @Test
    public void startWildcard() {
        final LocationMatcher m = new LocationMatcher("*pa");
        assertTrue(m.matches("pa"));
        assertTrue(m.matches("xxxpa"));
        assertFalse(m.matches("paxxx"));
    }

    @Test
    public void endWildcard() {
        final LocationMatcher m = new LocationMatcher("pa*");
        assertTrue(m.matches("pa"));
        assertFalse(m.matches("xxxpa"));
        assertTrue(m.matches("paxxx"));
    }

    @Test
    public void bothWildcard() {
        final LocationMatcher m = new LocationMatcher("*pa*");
        assertTrue(m.matches("pa"));
        assertTrue(m.matches("xxxpa"));
        assertTrue(m.matches("paxxx"));
        assertFalse(m.matches(""));
    }

    @Test
    public void onlyWildcard() {
        final LocationMatcher m = new LocationMatcher("*");
        assertTrue(m.matches("pa"));
        assertTrue(m.matches(""));
    }

    @Test
    public void packageAndClass() {
        final LocationMatcher m = new LocationMatcher("a.B");
        assertTrue(m.matches("a", "B"));
        assertFalse(m.matches("a"));
        assertFalse(m.matches("a", "C"));
    }

    @Test
    public void wildcardPackageAndClass2() {
        final LocationMatcher m = new LocationMatcher("a*.B");
        assertTrue(m.matches("a", "B"));
        assertTrue(m.matches("ab", "B"));
        assertTrue(m.matches("a.b", "B"));
        assertFalse(m.matches("b", "B"));
        assertFalse(m.matches("a", "C"));
    }

    @Test
    public void packageAndWildcardClass() {
        final LocationMatcher m = new LocationMatcher("a.*B");
        assertTrue(m.matches("a", "B"));
        assertTrue(m.matches("a", "xxxB"));
        assertFalse(m.matches("a"));
        assertFalse(m.matches("a", "C"));
    }

    @Test
    public void wildcardPackageAndWildcardClass() {
        final LocationMatcher m = new LocationMatcher("a*.*B");
        assertTrue(m.matches("a", "B"));
        assertTrue(m.matches("ab", "B"));
        assertTrue(m.matches("a.b", "B"));
        assertTrue(m.matches("a", "xxxB"));
        assertFalse(m.matches("a"));
        assertFalse(m.matches("b", "B"));
        assertFalse(m.matches("a", "C"));
    }


}