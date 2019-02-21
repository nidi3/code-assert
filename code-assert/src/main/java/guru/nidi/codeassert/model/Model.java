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
import static java.util.Collections.emptyList;

public class Model {
    public static final String UNNAMED_PACKAGE = "<Unnamed Package>";

    final Map<String, CodePackage> packages = new HashMap<>();
    final Map<String, CodeClass> classes = new HashMap<>();
    private final Set<String> ownPackages = new HashSet<>();
    private final List<String> ignorePackages;
    private final List<String> mergePackages;

    Model() {
        this(emptyList(), emptyList());
    }

    Model(List<String> ignorePackages, List<String> mergePackages) {
        this.ignorePackages = ignorePackages;
        this.mergePackages = mergePackages;
    }

    public static ModelBuilder from(File... files) {
        return from(asList(files));
    }

    public static ModelBuilder from(List<File> files) {
        return new ModelBuilder().and(files);
    }

    public Model read(List<File> files) {
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
            final CodeClass clazz = parser.parse(in, this);
            if (clazz != null) {
                ownPackages.add(clazz.getPackageName());
            }
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
        if (isIgnoreClass(name)) {
            return null;
        }
        CodeClass clazz = classes.get(name);
        if (clazz == null) {
            final CodePackage pack = getOrCreatePackage(packageOf(name));
            clazz = new CodeClass(name, pack);
            classes.put(name, clazz);
            pack.addClass(clazz);
        }
        return clazz;
    }

    String packageOf(String type) {
        final int pos = type.lastIndexOf('.');
        final String pack = pos < 0 ? UNNAMED_PACKAGE : type.substring(0, pos);
        return mergePackages.stream().filter(pack::startsWith).findFirst().orElse(pack);
    }

    boolean isIgnoreClass(String name) {
        return ignorePackages.stream().anyMatch(name::startsWith);
    }

    public boolean isOwnPackage(CodePackage pack) {
        return ownPackages.contains(pack.getName());
    }

    public Collection<CodePackage> getPackages() {
        return packages.values();
    }

    public Collection<CodeClass> getClasses() {
        return classes.values();
    }

}
