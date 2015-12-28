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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class ClassFileParserTest {
    private ClassFileParser parser;

    @Before
    public void setUp() {
        parser = new ClassFileParser();
    }

    @Test(expected = IOException.class)
    public void invalidClassFile() throws IOException {
        parser.parse(Path.testJava("ExampleTest"));
    }

    @Test
    public void validClass() throws IOException {
        final JavaClass clazz = parser.parse(Path.testClass("ExampleConcreteClass"));

        assertEquals("guru.nidi.codeassert.model.ExampleConcreteClass", clazz.getName());
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());

        final Collection imports = clazz.getImports();
        assertEquals(19, imports.size());

        assertTrue(imports.contains(new JavaPackage("java.net")));
        assertTrue(imports.contains(new JavaPackage("java.text")));
        assertTrue(imports.contains(new JavaPackage("java.sql")));
        assertTrue(imports.contains(new JavaPackage("java.lang")));
        assertTrue(imports.contains(new JavaPackage("java.io")));
        assertTrue(imports.contains(new JavaPackage("java.rmi")));
        assertTrue(imports.contains(new JavaPackage("java.util")));
        assertTrue(imports.contains(new JavaPackage("java.util.jar")));
        assertTrue(imports.contains(new JavaPackage("java.math")));

        // annotations
        assertTrue(imports.contains(new JavaPackage("org.junit.runners")));
        assertTrue(imports.contains(new JavaPackage("java.applet")));
        assertTrue(imports.contains(new JavaPackage("org.junit")));
        assertTrue(imports.contains(new JavaPackage("javax.crypto")));
        assertTrue(imports.contains(new JavaPackage("java.awt.geom")));
        assertTrue(imports.contains(new JavaPackage("java.awt.image.renderable")));
        assertTrue(imports.contains(new JavaPackage("guru.nidi.codeassert.model.p1")));
        assertTrue(imports.contains(new JavaPackage("guru.nidi.codeassert.model.p2")));
        assertTrue(imports.contains(new JavaPackage("java.awt.im")));
        assertTrue(imports.contains(new JavaPackage("java.awt.dnd.peer")));
    }

    @Test
    public void innerClass() throws IOException {
        final JavaClass clazz = parser.parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));

        assertEquals("guru.nidi.codeassert.model.ExampleConcreteClass$ExampleInnerClass", clazz.getName());
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());

        final Collection imports = clazz.getImports();
        assertEquals(1, imports.size());
        assertTrue(imports.contains(new JavaPackage("java.lang")));
    }

    @Test
    public void packageClass() throws IOException {
        final JavaClass clazz = parser.parse(Path.testClass("ExamplePackageClass"));

        assertEquals("guru.nidi.codeassert.model.ExamplePackageClass", clazz.getName());
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());

        final Collection imports = clazz.getImports();
        assertEquals(1, imports.size());
        assertTrue(imports.contains(new JavaPackage("java.lang")));
    }

    @Test
    public void exampleClassFileFromTimDrury() throws IOException {
        parser.parse(ClassFileParser.class.getResourceAsStream("/example_class1.bin"));
    }

    @Test
    public void exampleClassFile2() throws IOException {
        parser.parse(ClassFileParser.class.getResourceAsStream("/example_class2.bin"));
    }

    @Test
    public void genericParameters() throws IOException {
        final JavaClass generic = parser.parse(Path.testClass("p4/GenericParameters"));
        final JavaClass subGeneric = parser.parse(Path.testClass("p4/SubGenericParameters"));

        assertEquals(11, generic.getImports().size());
        assertEquals(1, subGeneric.getImports().size());
    }
}

