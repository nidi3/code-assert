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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
public class ClassFileParserTest {
    private ClassFileParser parser;

    @BeforeEach
    void setUp() {
        parser = new ClassFileParser();
    }

    @Test
    void invalidClassFile() throws IOException {
        assertThrows(IOException.class, new Executable() {
            public void execute() throws Throwable {
                ClassFileParserTest.this.parse(Path.testJava("ExampleTest"));
            }
        });
    }

    @Test
    void className() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals(model("ExampleConcreteClass"), clazz.getName());
    }

    @Test
    void classSource() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    void size() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals(518, clazz.getCodeSize());
        assertEquals(2221, clazz.getTotalSize());
    }

    @Test
    void fields() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals(1, clazz.getFields().size());
        final MemberInfo field = clazz.getFields().get(0);
        assertEquals(2, field.getAccessFlags());
        assertEquals(0, field.getCodeSize());
        assertEquals("statements", field.getName());
    }

    @Test
    void methods() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertEquals(7, clazz.getMethods().size());
        final MemberInfo method = clazz.getMethods().get(0);
        assertEquals(1, method.getAccessFlags());
        assertEquals(51, method.getCodeSize());
        assertEquals("<init>", method.getName());
    }

    @Test
    void classImports() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertCollectionEquals(toString(clazz.usedForeignPackages()),
                "java.net", "java.text", "java.sql", "java.lang", "java.io",
                "java.rmi", "java.util", "java.util.jar", "java.math",

                // annotations
                "org.junit.jupiter.api",
                "java.awt.geom", "java.awt.image.renderable", "java.awt.im", "java.awt",
                model("p1"), model("p2"));
    }

    @Test
    void classImportCounts() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertCollectionEquals(toString(clazz.usedPackageCounts()),
                "java.net 1", "java.text 1", "java.sql 1", "java.lang 5", "java.io 3",
                "java.rmi 1", "java.util 1", "java.util.jar 1", "java.math 1",

                // annotations
                "org.junit.jupiter.api 3",
                "java.awt 1", "java.awt.geom 1", "java.awt.image.renderable 1", "java.awt.im 1",
                "guru.nidi.codeassert.model 4", model("p1") + " 1", model("p2") + " 1");
    }

    @Test
    void usedClasses() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertCollectionEquals(toString(clazz.usedClasses()),
                "java.lang.String", "java.lang.Exception", "java.io.IOException", "java.io.File",
                "java.math.BigDecimal", "java.util.Vector", "java.util.jar.JarFile", "java.net.URL",
                "java.awt.im.InputContext", "java.awt.geom.AffineTransform",
                "java.awt.image.renderable.ContextualRenderedImageFactory", "java.awt.Checkbox",
                "java.rmi.RemoteException", "java.sql.Statement",
                "java.text.NumberFormat",
                "org.junit.jupiter.api.Tag", "org.junit.jupiter.api.Disabled", "org.junit.jupiter.api.Test",
                model("p2.ExampleEnum"), model("ExampleAbstractClass"), model("p1.ExampleInnerAnnotation"),
                model("ExampleConcreteClass$ExampleInnerClass"), model("ExampleAnnotation"));
    }

    @Test
    void usedClassCount() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass"));
        assertCollectionEquals(toString(clazz.usedClassCounts()),
                "java.lang.String 3", "java.lang.Exception 2", "java.io.IOException 1", "java.io.File 2",
                "java.math.BigDecimal 1", "java.util.Vector 1", "java.util.jar.JarFile 1", "java.net.URL 1",
                "java.awt.im.InputContext 1", "java.awt.geom.AffineTransform 1",
                "java.awt.image.renderable.ContextualRenderedImageFactory 1", "java.awt.Checkbox 1",
                "java.rmi.RemoteException 1", "java.sql.Statement 1",
                "java.text.NumberFormat 1",
                "org.junit.jupiter.api.Tag 1", "org.junit.jupiter.api.Disabled 1", "org.junit.jupiter.api.Test 1",
                model("p2.ExampleEnum 1"), model("ExampleAbstractClass 2"), model("p1.ExampleInnerAnnotation 1"),
                model("ExampleConcreteClass$ExampleInnerClass 1"), model("ExampleAnnotation 1"));
    }

    @Test
    void innerClassName() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertEquals(model("ExampleConcreteClass$ExampleInnerClass"), clazz.getName());
    }

    @Test
    void innerClassSource() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    void innerClassImports() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExampleConcreteClass$ExampleInnerClass"));
        assertCollectionEquals(clazz.usedForeignPackages(), new CodePackage("java.lang"));
    }

    @Test
    void packageClassName() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExamplePackageClass"));
        assertEquals(model("ExamplePackageClass"), clazz.getName());
    }

    @Test
    void packageClassSource() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExamplePackageClass"));
        assertEquals("ExampleConcreteClass.java", clazz.getSourceFile());
    }

    @Test
    void packageClassImports() throws IOException {
        final CodeClass clazz = parse(Path.testClass("ExamplePackageClass"));
        assertCollectionEquals(clazz.usedPackages(), new CodePackage("java.lang"));
    }

    @Test
    void exampleClassFileFromTimDrury() throws IOException {
        parser.parse(ClassFileParser.class.getResourceAsStream("/example_class1.bin"), new Model());
    }

    @Test
    void exampleClassFile2() throws IOException {
        parser.parse(ClassFileParser.class.getResourceAsStream("/example_class2.bin"), new Model());
    }

    @Test
    void genericParameters() throws IOException {
        final CodeClass generic = parse(Path.testClass("p4/GenericParameters"));
        assertCollectionEquals(toString(generic.usedPackages()),
                "java.util", "java.lang",
                model("p4.p4"), model("p4.p2"), model("p4.p1"), model("p4.p9"), model("p4.p10"),
                model("p4.p5"), model("p4.p6"), model("p4.p7"), model("p4.p8"));
    }

    @Test
    void subGenericParameters() throws IOException {
        final CodeClass subGeneric = parse(Path.testClass("p4/SubGenericParameters"));
        assertCollectionEquals(toString(subGeneric.usedForeignPackages()), model("p4.p1"));
    }

    @Test
    void classAnnotation() throws IOException {
        final CodeClass annotations = parse(Path.testClass("p5/Annotations"));
        assertCollectionEquals(toString(annotations.getAnnotations()),
                model("p5.ClassRetentionAnnotation"),
                model("ExampleAnnotation"),
                model("p1.ExampleInnerAnnotation"));
    }

    @Test
    void packageAnnotation() throws IOException {
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
        assertEquals(new HashSet<>(asList(expected)), new HashSet<>(actual));
    }

    private List<String> toString(Collection<?> cs) {
        final List<String> res = new ArrayList<>();
        for (final Object c : cs) {
            res.add(c.toString());
        }
        return res;
    }

    private List<String> toString(Map<?, Integer> cs) {
        final List<String> res = new ArrayList<>();
        for (final Map.Entry<?, Integer> c : cs.entrySet()) {
            res.add(c.getKey().toString() + " " + c.getValue());
        }
        return res;
    }

    private CodeClass parse(File f) throws IOException {
        return parser.parse(f, new Model());
    }
}

