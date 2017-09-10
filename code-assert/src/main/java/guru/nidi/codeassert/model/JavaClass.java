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

import guru.nidi.codeassert.config.LocationMatcher;
import guru.nidi.codeassert.util.CountSet;

import java.util.*;

/**
 * The <code>JavaClass</code> class represents a Java
 * class or interface.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
public class JavaClass extends UsingElement<JavaClass> {
    private final String name;
    private final JavaPackage pack;
    private final CountSet<JavaPackage> usedPackages;
    private final CountSet<JavaClass> usedClasses;
    private final Set<JavaClass> annotations;
    final List<MemberInfo> fields = new ArrayList<>();
    final List<MemberInfo> methods = new ArrayList<>();
    String sourceFile;
    int codeSize;
    int totalSize;

    JavaClass(String name, JavaPackage pack) {
        this.name = name;
        this.pack = pack;
        usedPackages = new CountSet<>();
        usedClasses = new CountSet<>();
        annotations = new HashSet<>();
        sourceFile = "Unknown";
    }

    public String getName() {
        return name;
    }

    public JavaPackage getPackage() {
        return pack;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Set<JavaClass> getAnnotations() {
        return annotations;
    }

    public List<MemberInfo> getFields() {
        return fields;
    }

    public List<MemberInfo> getMethods() {
        return methods;
    }

    public int getCodeSize() {
        return codeSize;
    }

    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public JavaClass self() {
        return this;
    }

    public Collection<JavaPackage> usedForeignPackages() {
        final Set<JavaPackage> res = new HashSet<>(usedPackages());
        res.remove(pack);
        return res;
    }

    public Collection<JavaPackage> usedPackages() {
        return usedPackages.asSet();
    }

    public Map<JavaPackage, Integer> usedPackageCounts() {
        return usedPackages.asMap();
    }

    public Collection<JavaClass> usedClasses() {
        return usedClasses.asSet();
    }

    public Map<JavaClass, Integer> usedClassCounts() {
        return usedClasses.asMap();
    }

    public boolean uses(JavaPackage pack) {
        return usedPackages.contains(pack);
    }

    @Override
    public Collection<JavaClass> uses() {
        return usedClasses();
    }

    @Override
    public String getPackageName() {
        return pack.getName();
    }

    @Override
    public Collection<String> usedVia(UsingElement<JavaClass> other) {
        return Collections.emptyList();
    }

    @Override
    public boolean isMatchedBy(LocationMatcher matcher) {
        return matcher.matchesClass(name);
    }

    void addImport(String type, Model model) {
        if (!name.equals(type)) {
            final String packName = Model.packageOf(type);
            final JavaPackage p = model.getOrCreatePackage(packName);
            usedPackages.add(p);
            pack.addEfferent(p);
            usedClasses.add(model.getOrCreateClass(type));
        }
    }

    void addAnnotation(String type, Model model) {
        addImport(type, model);
        annotations.add(model.getOrCreateClass(type));
    }

    public boolean equals(Object other) {
        if (other instanceof JavaClass) {
            final JavaClass otherClass = (JavaClass) other;
            return otherClass.getName().equals(getName());
        }
        return false;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
