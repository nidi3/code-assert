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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

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
        assertEquals(model("ExampleConcreteClass"), clazz.getName());
    }

    @Test
    public void classSource() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    public void classImports() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertCollectionEquals(toString(clazz.getImports()),
                "java.net", "java.text", "java.sql", "java.lang", "java.io",
                "java.rmi", "java.util", "java.util.jar", "java.math",

                // annotations
                "org.junit.runners", "org.junit",
                "java.applet", "java.awt.geom", "java.awt.image.renderable",
                "java.awt.im", "java.awt.dnd.peer", "javax.crypto",
                model("p1"), model("p2"));
    }

    @Test
    public void innerClassName() throws IOException {
        final JavaClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertEquals(model("ExampleConcreteClass$ExampleInnerClass"), clazz.getName());
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
        assertEquals(model("ExamplePackageClass"), clazz.getName());
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
        assertCollectionEquals(toString(generic.getImports()),
                "java.util", "java.lang",
                model("p4.p4"), model("p4.p2"), model("p4.p1"), model("p4.p9"), model("p4.p10"),
                model("p4.p5"), model("p4.p6"), model("p4.p7"), model("p4.p8"));
    }

    @Test
    public void subGenericParameters() throws IOException {
        final JavaClass subGeneric = parse(Path.testClass("p4/SubGenericParameters"));
        assertCollectionEquals(toString(subGeneric.getImports()), model("p4.p1"));
    }

    @Test
    public void classAnnotation() throws IOException {
        final JavaClass annotations = parse(Path.testClass("p5/Annotations"));
        assertCollectionEquals(toString(annotations.getAnnotations()),
                model("p5.ClassRetentionAnnotation"),
                model("ExampleAnnotation"),
                model("p1.ExampleInnerAnnotation"));
    }

    @Test
    public void packageAnnotation() throws IOException {
        final Model model = new Model();
        parser.parse(Path.testClass("p5/package-info"), model);
        assertCollectionEquals(toString(model.getOrCreatePackage(model("p5")).getAnnotations()),
                model("p5.ClassRetentionAnnotation"),
                model("ExampleAnnotation"),
                model("p1.ExampleInnerAnnotation"));
    }

    private String model(String s) {
        return "guru.nidi.codeassert.model." + s;
    }

    @SafeVarargs
    private static <T> void assertCollectionEquals(Collection<T> actual, T... expected) {
        assertEquals(new HashSet<T>(Arrays.asList(expected)), new HashSet<T>(actual));
    }

    private List<String> toString(Collection<?> cs) {
        final List<String> res = new ArrayList<>();
        for (final Object c : cs) {
            res.add(c.toString());
        }
        return res;
    }

    private JavaClass parse(File f) throws IOException {
        return parser.parse(f, new Model());
    }
}

