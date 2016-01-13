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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
        parse(Path.testJava("ExampleTest"));
    }

    @Test
    public void className() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals("guru.nidi.codeassert.model.ExampleConcreteClass", clazz.getName());
    }

    @Test
    public void classSource() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    public void classImports() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertCollectionEquals(clazz.getImports(),
                new JavaPackage("java.net"),
                new JavaPackage("java.text"),
                new JavaPackage("java.sql"),
                new JavaPackage("java.lang"),
                new JavaPackage("java.io"),
                new JavaPackage("java.rmi"),
                new JavaPackage("java.util"),
                new JavaPackage("java.util.jar"),
                new JavaPackage("java.math"),

                // annotations
                new JavaPackage("org.junit.runners"),
                new JavaPackage("java.applet"),
                new JavaPackage("org.junit"),
                new JavaPackage("javax.crypto"),
                new JavaPackage("java.awt.geom"),
                new JavaPackage("java.awt.image.renderable"),
                new JavaPackage("guru.nidi.codeassert.model.p1"),
                new JavaPackage("guru.nidi.codeassert.model.p2"),
                new JavaPackage("java.awt.im"),
                new JavaPackage("java.awt.dnd.peer"));
    }

    @Test
    public void innerClassName() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertEquals("guru.nidi.codeassert.model.ExampleConcreteClass$ExampleInnerClass", clazz.getName());
    }

    @Test
    public void innerClassSource() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    public void innerClassImports() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertCollectionEquals(clazz.getImports(), new JavaPackage("java.lang"));
    }

    @Test
    public void packageClassName() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExamplePackageClass"));
        assertEquals("guru.nidi.codeassert.model.ExamplePackageClass", clazz.getName());
    }

    @Test
    public void packageClassSource() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExamplePackageClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    public void packageClassImports() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExamplePackageClass"));
        assertCollectionEquals(clazz.getImports(), new JavaPackage("java.lang"));
    }

    @Test
    public void exampleClassFileFromTimDrury() throws IOException {
        parser.parse(ClassFileParser.class.getResourceAsStream("/example_class1.bin"), new Model());
    }

    @Test
    public void exampleClassFile2() throws IOException {
        parser.parse(ClassFileParser.class.getResourceAsStream("/example_class2.bin"), new Model());
    }

    @Test
    public void genericParameters() throws IOException {
        final JavaClass generic = parse(Path.testClass("p4/GenericParameters"));
        assertEquals(11, generic.getImports().size());
    }

    @Test
    public void subGenericParameters() throws IOException {
        final JavaClass subGeneric = parse(Path.testClass("p4/SubGenericParameters"));
        assertEquals(1, subGeneric.getImports().size());
    }

    @SafeVarargs
    private static <T> void assertCollectionEquals(Collection<T> actual, T... expected) {
        assertEquals(expected.length, actual.size());
        assertTrue(actual.containsAll(Arrays.asList(expected)));
    }

    private JavaClass parse(File f) throws IOException {
        return parser.parse(f, new Model());
    }
}

