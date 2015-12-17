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


import guru.nidi.codeassert.model.p1.ExampleInnerAnnotation;
import guru.nidi.codeassert.model.p2.ExampleEnum;

import java.math.BigDecimal;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

@org.junit.runners.Suite.SuiteClasses(java.applet.AppletStub.class)
public class ExampleConcreteClass extends ExampleAbstractClass {

    private java.sql.Statement[] statements;

    public ExampleConcreteClass() {
    }

    public void a() {
        try {
            java.net.URL url = new java.net.URL("http://www.clarkware.com");
        } catch (Exception e) {
        }
    }

    public java.util.Vector b(String[] s, java.text.NumberFormat nf) {
        return null;
    }

    public void c(BigDecimal bd, byte[] bytes) throws java.rmi.RemoteException {
        int[] a = {1, 2, 3};
        int[][] b = {{1, 2}, {3, 4}, {5, 6}};
    }

    public java.io.File[] d() throws java.io.IOException {
        java.util.jar.JarFile[] files = new java.util.jar.JarFile[1];
        return new java.io.File[10];
    }

    public java.lang.String[] e() {
        return new String[1];
    }

    @org.junit.Test(expected = javax.crypto.BadPaddingException.class)
    @ExampleAnnotation(
            c1 = java.awt.geom.AffineTransform.class,
            c2 = java.awt.image.renderable.ContextualRenderedImageFactory.class,
            c3 = @ExampleInnerAnnotation({
                    java.awt.im.InputContext.class,
                    java.awt.dnd.peer.DragSourceContextPeer.class}),
            c4 = ExampleEnum.E1)
    @org.junit.Ignore
    public void f() {
    }

    public class ExampleInnerClass {
    }
}

class ExamplePackageClass {
}