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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        assertTrue(m.matchesPackage("pa"));
        assertTrue(m.matchesPackageClass("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
    }

    @Test
    public void methodOnly() {
        final LocationMatcher m = new LocationMatcher("#me");
        assertFalse(m.matchesPackage("pa"));
        assertFalse(m.matchesPackageClass("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matches("pa", "Cl", "mu"));
    }

    @Test
    public void classOnlyUppercase() {
        final LocationMatcher m = new LocationMatcher("Cl");
        assertFalse(m.matchesPackage("pa"));
        assertTrue(m.matchesPackageClass("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matchesPackageClass("pa", "Co"));
    }

    @Test
    public void classOnlySlash() {
        final LocationMatcher m = new LocationMatcher("/Cl");
        assertFalse(m.matchesPackage("pa"));
        assertTrue(m.matchesPackageClass("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matchesPackageClass("pa", "Co"));
    }

    @Test
    public void classOnlyInner() {
        final LocationMatcher m = new LocationMatcher("Cl");
        assertTrue(m.matchesClass("Cl$bla"));
        assertTrue(m.matchesPackageClass("pa", "Cl$blu"));
        assertTrue(m.matches("pa", "Cl$blo", ""));
    }

    @Test
    public void classMethodInner() {
        final LocationMatcher m = new LocationMatcher("Cl#me");
        assertFalse(m.matches("pa", "Cl$bla", "me"));
    }

    @Test
    public void packageOnly() {
        final LocationMatcher m = new LocationMatcher("pa");
        assertTrue(m.matchesPackage("pa"));
        assertTrue(m.matchesPackageClass("pa", "Cl"));
        assertTrue(m.matches("pa", "Cl", "me"));
        assertFalse(m.matchesPackage("po"));
    }

    @Test
    public void startWildcard() {
        final LocationMatcher m = new LocationMatcher("*pa");
        assertTrue(m.matchesPackage("pa"));
        assertTrue(m.matchesPackage("xxxpa"));
        assertFalse(m.matchesPackage("paxxx"));
    }

    @Test
    public void endWildcard() {
        final LocationMatcher m = new LocationMatcher("pa*");
        assertTrue(m.matchesPackage("pa"));
        assertFalse(m.matchesPackage("xxxpa"));
        assertTrue(m.matchesPackage("paxxx"));
    }

    @Test
    public void bothWildcard() {
        final LocationMatcher m = new LocationMatcher("*pa*");
        assertTrue(m.matchesPackage("pa"));
        assertTrue(m.matchesPackage("xxxpa"));
        assertTrue(m.matchesPackage("paxxx"));
        assertFalse(m.matchesPackage(""));
    }

    @Test
    public void onlyWildcard() {
        final LocationMatcher m = new LocationMatcher("*");
        assertTrue(m.matchesPackage("pa"));
        assertTrue(m.matchesPackage(""));
    }

    @Test
    public void packageAndClass() {
        final LocationMatcher m = new LocationMatcher("a.B");
        assertTrue(m.matchesPackageClass("a", "B"));
        assertFalse(m.matchesPackage("a"));
        assertFalse(m.matchesPackageClass("a", "C"));
    }

    @Test
    public void wildcardPackageAndClass2() {
        final LocationMatcher m = new LocationMatcher("a*.B");
        assertTrue(m.matchesPackageClass("a", "B"));
        assertTrue(m.matchesPackageClass("ab", "B"));
        assertTrue(m.matchesPackageClass("a.b", "B"));
        assertFalse(m.matchesPackageClass("b", "B"));
        assertFalse(m.matchesPackageClass("a", "C"));
    }

    @Test
    public void packageAndWildcardClass() {
        final LocationMatcher m = new LocationMatcher("a.*B");
        assertTrue(m.matchesPackageClass("a", "B"));
        assertTrue(m.matchesPackageClass("a", "xxxB"));
        assertFalse(m.matchesPackage("a"));
        assertFalse(m.matchesPackageClass("a", "C"));
    }

    @Test
    public void wildcardPackageAndWildcardClass() {
        final LocationMatcher m = new LocationMatcher("a*.*B");
        assertTrue(m.matchesPackageClass("a", "B"));
        assertTrue(m.matchesPackageClass("ab", "B"));
        assertTrue(m.matchesPackageClass("a.b", "B"));
        assertTrue(m.matchesPackageClass("a", "xxxB"));
        assertFalse(m.matchesPackage("a"));
        assertFalse(m.matchesPackageClass("b", "B"));
        assertFalse(m.matchesPackageClass("a", "C"));
    }

}
