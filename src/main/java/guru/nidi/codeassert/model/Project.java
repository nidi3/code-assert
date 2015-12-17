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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Project {
    private final Map<String, JavaPackage> packages = new HashMap<>();

    public Collection<JavaPackage> getPackages() {
        return packages.values();
    }

    public JavaPackage getPackage(String name) {
        return packages.get(name);
    }

    public JavaPackage addPackage(String name) {
        JavaPackage pkg = packages.get(name);
        if (pkg == null) {
            pkg = new JavaPackage(name);
            addPackage(pkg);
        }
        return pkg;
    }

    public void addPackages(Collection<JavaPackage> packages) {
        for (JavaPackage pkg : packages) {
            addPackage(pkg);
        }
    }

    public void addPackage(JavaPackage pkg) {
        if (!packages.containsValue(pkg)) {
            packages.put(pkg.getName(), pkg);
        }
    }

    public FileSource fromCode(String... directoryOrJar) throws IOException {
        return new FileSource(directoryOrJar);
    }

    public class FileSource {
        private final FileManager fileManager;

        private FileSource(String[] code) throws IOException {
            fileManager = new FileManager().withDirectories(code);
        }

        public Collection<JavaPackage> readPackages(PackageCollector collector) {
            final Collection<JavaClass> classes = new JavaClassBuilder(new ClassFileParser(collector), fileManager).build();
            for (JavaClass aClass : classes) {
                readClass(aClass, collector);
            }
            return getPackages();
        }

        private void readClass(JavaClass clazz, PackageCollector collector) {
            String packageName = clazz.getPackageName();

            if (!collector.accept(packageName)) {
                return;
            }

            JavaPackage clazzPackage = addPackage(packageName);
            clazzPackage.addClass(clazz);

            for (JavaPackage importedPackage : clazz.getImports()) {
                importedPackage = addPackage(importedPackage.getName());
                clazzPackage.dependsUpon(importedPackage);
            }
        }
    }
}
