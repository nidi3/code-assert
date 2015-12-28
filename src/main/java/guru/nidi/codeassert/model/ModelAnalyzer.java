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

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ModelAnalyzer implements Analyzer<Collection<JavaPackage>> {
    private final AnalyzerConfig config;

    public ModelAnalyzer(AnalyzerConfig config) {
        this.config = config;
    }

    public Collection<JavaPackage> analyze() {
        try {
            final Collection<JavaClass> classes = new JavaClassBuilder(
                    new ClassFileParser(config.getCollector()),
                    new FileManager().withDirectories(config.getClasses())).build();
            final Map<String, JavaPackage> packages = new HashMap<>();
            for (final JavaClass aClass : classes) {
                readClass(packages, aClass);
            }
            return packages.values();
        } catch (IOException e) {
            throw new RuntimeException("Problem executing ModelAnalyzer", e);
        }
    }

    private void readClass(Map<String, JavaPackage> packages, JavaClass clazz) {
        final String packageName = clazz.getPackageName();

        if (!config.getCollector().accept(packageName)) {
            return;
        }

        final JavaPackage clazzPackage = addPackage(packages, packageName);
        clazzPackage.addClass(clazz);

        for (JavaPackage importedPackage : clazz.getImports()) {
            importedPackage = addPackage(packages, importedPackage.getName());
            clazzPackage.dependsUpon(importedPackage);
        }
    }

    private JavaPackage addPackage(Map<String, JavaPackage> packages, String name) {
        JavaPackage pkg = packages.get(name);
        if (pkg == null) {
            pkg = new JavaPackage(name);
            packages.put(pkg.getName(), pkg);
        }
        return pkg;
    }
}
