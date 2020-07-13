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


import java.math.BigDecimal;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public abstract class ExampleAbstractClass implements ExampleInterface, java.io.Serializable {

    private java.lang.reflect.Method method;

    public ExampleAbstractClass() {
    }

    public void a() {
        java.net.URL url;
    }

    public java.util.Vector b(String[] s, java.text.NumberFormat nf) {
        return null;
    }

    public void c(BigDecimal bd, byte[] b) throws java.rmi.RemoteException {

    }

    public abstract java.io.File[] d() throws java.io.IOException;
}
