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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JarFileParserTest {
    @Test(expected = IOException.class)
    public void invalidJarFile() throws IOException {
        new JavaClassBuilder(new FileManager()
                .withFile(Path.testResource("bogus.jar").getAbsolutePath()))
                .build();
    }

    @Test(expected = IOException.class)
    public void testInvalidZipFile() throws IOException {
        new JavaClassBuilder(new FileManager()
                .withFile(Path.testResource("bogus.zip").getAbsolutePath()))
                .build();
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
        final Model model = new JavaClassBuilder(new FileManager().withFile(archive.getAbsolutePath())).build();
        final Map<String, JavaClass> classes = model.classes;
        assertEquals(19, classes.size());
        assertClassesExist(classes.values());
        assertInnerClassesExist(classes.values());
    }

    private void assertClassesExist(Collection classes) {
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleAbstractClass", new JavaPackage(""))));
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleInterface", new JavaPackage(""))));
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleConcreteClass", new JavaPackage(""))));
    }

    private void assertInnerClassesExist(Collection classes) {
        assertTrue(classes.contains(new JavaClass("jdepend.framework.ExampleConcreteClass$ExampleInnerClass", new JavaPackage(""))));
    }
}
