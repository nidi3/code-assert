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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Project {
    private final Map<String, JavaPackage> packages;
    private final PackageFilter filter;
    private final JavaClassBuilder builder;

    public Project(FileManager fileManager) {
        this(fileManager, PackageFilter.all());
    }

    public Project(FileManager fileManager, PackageFilter filter) {
        this.filter = filter;
        this.packages = new HashMap<>();
        this.builder = new JavaClassBuilder(new ClassFileParser(filter), fileManager);
    }

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

    public PackageFilter getFilter() {
        return filter;
    }

    public Collection<JavaPackage> read() {
        Collection<JavaClass> classes = builder.build();
        for (JavaClass aClass : classes) {
            readClass(aClass);
        }
        return getPackages();
    }

    private void readClass(JavaClass clazz) {
        String packageName = clazz.getPackageName();

        if (!getFilter().accept(packageName)) {
            return;
        }

        JavaPackage clazzPackage = addPackage(packageName);
        clazzPackage.addClass(clazz);

        for (JavaPackage importedPackage : clazz.getImportedPackages()) {
            importedPackage = addPackage(importedPackage.getName());
            clazzPackage.dependsUpon(importedPackage);
        }
    }
}
