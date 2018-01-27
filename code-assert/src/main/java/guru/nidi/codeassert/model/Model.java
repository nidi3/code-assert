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

import guru.nidi.codeassert.AnalyzerException;

import java.io.*;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import static java.util.Arrays.asList;

public class Model {
    public static final String UNNAMED_PACKAGE = "<Unnamed Package>";

    final Map<String, CodePackage> packages = new HashMap<>();
    final Map<String, CodeClass> classes = new HashMap<>();

    public static Model from(File... files) {
        return from(asList(files));
    }

    public static Model from(List<File> files) {
        return new Model().and(files);
    }

    public Model and(File... files) {
        return and(asList(files));
    }

    public Model and(List<File> files) {
        try {
            final ClassFileParser classParser = new ClassFileParser();
            for (final File file : files) {
                try (InputStream in = new FileInputStream(file)) {
                    add(classParser, file.getName(), in);
                }
            }
            return this;
        } catch (IOException e) {
            throw new AnalyzerException("Problem creating a Model", e);
        }
    }

    private void add(ClassFileParser parser, String name, InputStream in) throws IOException {
        if (name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".war") || name.endsWith(".ear")) {
            final JarInputStream jar = new JarInputStream(in);
            ZipEntry entry;
            while ((entry = jar.getNextEntry()) != null) {
                try {
                    if (!entry.isDirectory()) {
                        add(parser, entry.getName(), jar);
                    }
                } finally {
                    jar.closeEntry();
                }
            }
        } else if (name.endsWith(".class")) {
            parser.parse(in, this);
        }
    }

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

}
