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

import guru.nidi.codeassert.BaseProject;
import guru.nidi.codeassert.PackageCollector;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ModelProject extends BaseProject<Collection<JavaPackage>> {
    private final Map<String, JavaPackage> packages = new HashMap<>();

    public ModelProject(String codeLocation, PackageCollector collector) {
        super(codeLocation, collector);
    }

    public ModelProject(List<String> codeLocations, PackageCollector collector) {
        super(codeLocations, collector);
    }

    public ModelProject(BaseProject project) {
        super(project);
    }

    public Collection<JavaPackage> analyze() throws IOException {
        final Collection<JavaClass> classes = new JavaClassBuilder(new ClassFileParser(packageCollector), new FileManager().withDirectories(codeLocations)).build();
        for (JavaClass aClass : classes) {
            readClass(aClass);
        }
        return packages.values();
    }

    public Collection<JavaPackage> getPackages() {
        return packages.values();
    }

    private void readClass(JavaClass clazz) {
        String packageName = clazz.getPackageName();

        if (!packageCollector.accept(packageName)) {
            return;
        }

        JavaPackage clazzPackage = addPackage(packageName);
        clazzPackage.addClass(clazz);

        for (JavaPackage importedPackage : clazz.getImports()) {
            importedPackage = addPackage(importedPackage.getName());
            clazzPackage.dependsUpon(importedPackage);
        }
    }

    private JavaPackage addPackage(String name) {
        JavaPackage pkg = packages.get(name);
        if (pkg == null) {
            pkg = new JavaPackage(name);
            addPackage(pkg);
        }
        return pkg;
    }

    private void addPackage(JavaPackage pkg) {
        if (!packages.containsValue(pkg)) {
            packages.put(pkg.getName(), pkg);
        }
    }
}
