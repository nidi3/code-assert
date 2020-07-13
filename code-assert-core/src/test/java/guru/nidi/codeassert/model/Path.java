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

import java.io.File;

public final class Path {
    private static final String PACKAGE = "guru/nidi/codeassert/model/";
    private static final String TEST_RESOURCES = "src/test/resources/";
    private static final String TEST_JAVAS = "src/test/java/" + PACKAGE;
    public static final String TEST_CLASSES = "target/test-classes/" + PACKAGE;
    public static final String CLASSES = "target/classes/" + PACKAGE;

    private Path() {
    }

    public static File testJava(String name) {
        return new File(TEST_JAVAS + name + ".java");
    }

    public static File testClass(String name) {
        return new File(TEST_CLASSES + name + ".class");
    }

    public static File testResource(String name) {
        return new File(TEST_RESOURCES + name);
    }

    public static File clazz(String name) {
        return new File(CLASSES + name + ".class");
    }
}
