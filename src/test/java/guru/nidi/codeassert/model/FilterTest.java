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
package guru.nidi.codeassert.model;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class FilterTest {

    @Test
    public void collection() throws IOException {
        Collection<String> filters = Arrays.asList("java.*", "javax.*", "sun.*", "com.sun.*", "com.xyz.tests.*");
        PackageFilter filter = PackageFilter.all().excluding(filters);
        assertEquals(5, filter.getFilters().size());
        assertFiltersExist(filter);
    }

    @Test
    public void collectionSubset() {
        Collection<String> filters = new ArrayList<>();
        filters.add("com.xyz");
        PackageFilter filter = PackageFilter.all().excluding(filters);
        assertEquals(1, filter.getFilters().size());
    }

    @Test
    public void accept() {
        final PackageFilter filter = PackageFilter.all().excluding("a.b.c").including("a.b").excluding("a");
        assertFalse(filter.accept("a"));
        assertTrue(filter.accept("a.b"));
        assertTrue(filter.accept("a.b.d"));
        assertFalse(filter.accept("a.b.c"));
        assertFalse(filter.accept("a.c"));
    }

    private void assertFiltersExist(PackageFilter filter) {
        assertFalse(filter.accept("java.lang"));
        assertFalse(filter.accept("javax.ejb"));
        assertTrue(filter.accept("com.xyz.tests"));
        assertFalse(filter.accept("com.xyz.tests.a"));
        assertTrue(filter.accept("com.xyz.ejb"));
    }
}