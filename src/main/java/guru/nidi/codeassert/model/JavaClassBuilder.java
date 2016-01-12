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

import guru.nidi.codeassert.AnalyzerException;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * The <code>JavaClassBuilder</code> builds <code>JavaClass</code>
 * instances from .class, .jar, .war, or .zip files.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

class JavaClassBuilder {
    private final ClassFileParser parser;
    private final FileManager fileManager;
    final Map<String, JavaPackage> packages = new HashMap<>();
    final Map<String, JavaClass> classes = new HashMap<>();

    public JavaClassBuilder() {
        this(new ClassFileParser(), new FileManager());
    }

    public JavaClassBuilder(ClassFileParser parser, FileManager fm) {
        this.parser = parser;
        this.fileManager = fm;
    }

    JavaPackage getPackage(String name) {
        JavaPackage pack = packages.get(name);
        if (pack == null) {
            pack = new JavaPackage(name);
            packages.put(name, pack);
        }
        return pack;
    }

    JavaClass getClass(String name) {
        JavaClass clazz = classes.get(name);
        if (clazz == null) {
            final JavaPackage pack = getPackage(JavaClass.packageOf(name));
            clazz = new JavaClass(name, pack);
            classes.put(name, clazz);
            pack.addClass(clazz);
        }
        return clazz;
    }

    /**
     * Builds the <code>JavaClass</code> instances.
     *
     * @return Collection of <code>JavaClass</code> instances.
     */
    public void build() {
        for (final File nextFile : fileManager.extractFiles()) {
            try {
                buildClasses(nextFile);
            } catch (IOException e) {
                throw new AnalyzerException("could not parse class " + nextFile, e);
            }
        }
    }

    /**
     * Builds the <code>JavaClass</code> instances from the
     * specified file.
     *
     * @param file Class or Jar file.
     * @return Collection of <code>JavaClass</code> instances.
     */
    public void buildClasses(File file) throws IOException {
        if (fileManager.acceptClassFile(file)) {
            try (final InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                parser.parse(is, this);
            }
        } else if (fileManager.acceptJarFile(file)) {
            try (final JarFile jarFile = new JarFile(file)) {
                buildClasses(jarFile);
            }
        } else {
            throw new IOException("File is not a valid " + ".class, .jar, .war, or .zip file: " + file.getPath());
        }
    }

    /**
     * Builds the <code>JavaClass</code> instances from the specified
     * jar, war, or zip file.
     *
     * @param file Jar, war, or zip file.
     * @return Collection of <code>JavaClass</code> instances.
     */
    public void buildClasses(JarFile file) throws IOException {
        final Enumeration entries = file.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry e = (ZipEntry) entries.nextElement();
            if (fileManager.acceptClassFileName(e.getName())) {
                try (final InputStream is = file.getInputStream(e)) {
                    parser.parse(is, this);
                }
            }
        }
    }
}
