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

import static guru.nidi.codeassert.model.PackageCollector.all;
import static org.junit.Assert.*;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class CollectorTest {

    @Test
    public void collection() throws IOException {
        PackageCollector collector = all().excluding("java.*", "javax.*", "sun.*", "com.sun.*", "com.xyz.tests.*");
        assertEquals(5, collector.getCollectors().size());
        assertCollectorsExist(collector);
    }

    @Test
    public void accept() {
        final PackageCollector collector = all().excluding("a.b.c").including("a.b").excluding("a");
        assertFalse(collector.accept("a"));
        assertTrue(collector.accept("a.b"));
        assertTrue(collector.accept("a.b.d"));
        assertFalse(collector.accept("a.b.c"));
        assertFalse(collector.accept("a.c"));
    }

    private void assertCollectorsExist(PackageCollector collector) {
        assertFalse(collector.accept("java.lang"));
        assertFalse(collector.accept("javax.ejb"));
        assertTrue(collector.accept("com.xyz.tests"));
        assertFalse(collector.accept("com.xyz.tests.a"));
        assertTrue(collector.accept("com.xyz.ejb"));
    }
}