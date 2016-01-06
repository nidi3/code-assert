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

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
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

    public JavaClassBuilder() {
        this(new ClassFileParser(), new FileManager());
    }

    public JavaClassBuilder(ClassFileParser parser, FileManager fm) {
        this.parser = parser;
        this.fileManager = fm;
    }

    /**
     * Builds the <code>JavaClass</code> instances.
     *
     * @return Collection of <code>JavaClass</code> instances.
     */
    public Collection<JavaClass> build() {
        final Collection<JavaClass> classes = new ArrayList<>();
        for (final File nextFile : fileManager.extractFiles()) {
            try {
                classes.addAll(buildClasses(nextFile));
            } catch (IOException ioe) {
                System.err.println("\n" + ioe.getMessage());
            }
        }

        return classes;
    }

    /**
     * Builds the <code>JavaClass</code> instances from the
     * specified file.
     *
     * @param file Class or Jar file.
     * @return Collection of <code>JavaClass</code> instances.
     */
    public Collection<JavaClass> buildClasses(File file) throws IOException {
        if (fileManager.acceptClassFile(file)) {
            try (final InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                final JavaClass parsedClass = parser.parse(is);
                final Collection<JavaClass> javaClasses = new ArrayList<>();
                javaClasses.add(parsedClass);
                return javaClasses;
            }
        } else if (fileManager.acceptJarFile(file)) {
            try (final JarFile jarFile = new JarFile(file)) {
                return buildClasses(jarFile);
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
    public Collection<JavaClass> buildClasses(JarFile file) throws IOException {
        final Collection<JavaClass> javaClasses = new ArrayList<>();

        final Enumeration entries = file.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry e = (ZipEntry) entries.nextElement();
            if (fileManager.acceptClassFileName(e.getName())) {
                try (final InputStream is = file.getInputStream(e)) {
                    javaClasses.add(parser.parse(is));
                }
            }
        }

        return javaClasses;
    }
}
