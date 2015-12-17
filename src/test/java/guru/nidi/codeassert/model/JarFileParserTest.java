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

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JarFileParserTest {
    @Test(expected = IOException.class)
    public void invalidJarFile() throws IOException {
        JavaClassBuilder builder = new JavaClassBuilder();
        File bogusFile = Path.testResource("bogus.jar");
        builder.buildClasses(bogusFile);
    }

    @Test(expected = IOException.class)
    public void testInvalidZipFile() throws IOException {
        JavaClassBuilder builder = new JavaClassBuilder();
        File bogusFile = Path.testResource("bogus.zip");
        builder.buildClasses(bogusFile);
    }

    @Test
    public void jarFile() throws IOException {
        archive(Path.testResource("test.jar"));
    }

    @Test
    public void zipFile() throws IOException {
        archive(Path.testResource("test.zip"));
    }

    private void archive(File archive) throws IOException {
        JavaClassBuilder builder = new JavaClassBuilder();

        Collection classes = builder.buildClasses(archive);
        assertEquals(5, classes.size());

        assertClassesExist(classes);
        assertInnerClassesExist(classes);
    }

    private void assertClassesExist(Collection classes) {
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleAbstractClass")));
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleInterface")));
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleConcreteClass")));
    }

    private void assertInnerClassesExist(Collection classes) {
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleConcreteClass$ExampleInnerClass")));
    }
}