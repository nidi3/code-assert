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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import static java.util.Arrays.asList;

public class ModelBuilder {
    private final ClassFileParser classParser = new ClassFileParser();
    private final List<File> files = new ArrayList<>();
    private final Model model = new Model();

    private final Map<String, List<CodeClass>> classesBySource = new HashMap<>();

    public ModelBuilder files(File... files) {
        return files(asList(files));
    }

    public ModelBuilder files(List<File> files) {
        this.files.addAll(files);
        return this;
    }

    public Model build() {
        try {
            for (final File file : files) {
                if (isZip(file.getName()) || isClass(file.getName())) {
                    try (final InputStream in = new FileInputStream(file)) {
                        parseClass(file.getName(), in);
                    }
                }
            }
            for (final File file : files) {
                if (!isZip(file.getName()) && !isClass(file.getName())) {
                    parseSource(file);
                }
            }
            return model;
        } catch (IOException e) {
            throw new AnalyzerException("Problem creating a Model", e);
        }
    }

    private void parseSource(File file) throws IOException {
        final SourceFile source = SourceFileParser.parse(file, StandardCharsets.UTF_8);
        if (source != null) {
            model.sources.put(file.getName(), source);
            final List<CodeClass> classes = classesBySource.get(file.getName());
            if (classes != null) {
                source.classes.addAll(classes);
                for (final CodeClass clazz : classes) {
                    clazz.source = source;
                }
            }
        }
    }

    private void parseClass(String name, InputStream in) throws IOException {
        if (isZip(name)) {
            final JarInputStream jar = new JarInputStream(in);
            ZipEntry entry;
            while ((entry = jar.getNextEntry()) != null) {
                try {
                    if (!entry.isDirectory()) {
                        parseClass(entry.getName(), jar);
                    }
                } finally {
                    jar.closeEntry();
                }
            }
        } else {
            addClass(classParser.parse(in, model));
        }
    }

    private void addClass(CodeClass clazz) {
        final String name = clazz.getSourceFile();
        List<CodeClass> classes = classesBySource.get(name);
        if (classes == null) {
            classes = new ArrayList<>();
            classesBySource.put(name, classes);
        }
        classes.add(clazz);
    }

    private boolean isZip(String name) {
        return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".war") || name.endsWith(".ear");
    }

    private boolean isClass(String name) {
        return name.endsWith(".class");
    }

}
