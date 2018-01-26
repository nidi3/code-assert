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

import java.util.*;

public class Model {
    public static final String UNNAMED_PACKAGE = "<Unnamed Package>";

    final Map<String, CodePackage> packages = new HashMap<>();
    final Map<String, CodeClass> classes = new HashMap<>();
    final Map<String, SourceFile> sources = new HashMap<>();

    CodePackage getOrCreatePackage(String name) {
        CodePackage pack = packages.get(name);
        if (pack == null) {
            pack = new CodePackage(name);
            packages.put(name, pack);
        }
        return pack;
    }

    CodeClass getOrCreateClass(String name) {
        CodeClass clazz = classes.get(name);
        if (clazz == null) {
            final CodePackage pack = getOrCreatePackage(packageOf(name));
            clazz = new CodeClass(name, pack);
            classes.put(name, clazz);
            pack.addClass(clazz);
        }
        return clazz;
    }

    static String packageOf(String type) {
        final int pos = type.lastIndexOf('.');
        return pos < 0 ? UNNAMED_PACKAGE : type.substring(0, pos);
    }

    public Collection<CodePackage> getPackages() {
        return packages.values();
    }

    public Collection<CodeClass> getClasses() {
        return classes.values();
    }

    public Collection<SourceFile> getSources() {
        return sources.values();
    }
}
