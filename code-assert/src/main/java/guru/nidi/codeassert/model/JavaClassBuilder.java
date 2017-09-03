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
    private final Model model = new Model();

    JavaClassBuilder(FileManager fm) {
        this.parser = new ClassFileParser();
        this.fileManager = fm;
    }

    /**
     * Builds the <code>JavaClass</code> instances.
     */
    public Model build() {
        for (final File nextFile : fileManager.extractFiles()) {
            try {
                buildClasses(nextFile);
            } catch (IOException e) {
                throw new AnalyzerException("could not parse class " + nextFile, e);
            }
        }
        return model;
    }

    private void buildClasses(File file) throws IOException {
        if (fileManager.acceptClassFile(file)) {
            try (final InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                parser.parse(is, model);
            }
        } else if (fileManager.acceptJarFile(file)) {
            try (final JarFile jarFile = new JarFile(file)) {
                buildClasses(jarFile);
            }
        } else {
            throw new IOException("File is not a valid " + ".class, .jar, .war, or .zip file: " + file.getPath());
        }
    }

    private void buildClasses(JarFile file) throws IOException {
        final Enumeration entries = file.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry e = (ZipEntry) entries.nextElement();
            if (fileManager.acceptClassFileName(e.getName())) {
                try (final InputStream is = file.getInputStream(e)) {
                    parser.parse(is, model);
                }
            }
        }
    }
}
