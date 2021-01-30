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

import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.Collections.emptyList;

/**
 * The <code>JavaClass</code> class represents a Java
 * class or interface.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
public class CodeClass extends UsingElement<CodeClass> {
    private final String name;
    private final CodePackage pack;
    private final CountSet<CodePackage> usedPackages;
    private final CountSet<CodeClass> usedClasses;
    private final Set<CodeClass> annotations;
    final List<MemberInfo> fields = new ArrayList<>();
    final List<MemberInfo> methods = new ArrayList<>();
    String superClass;
    String sourceFile;
    int codeSize;
    int totalSize;
    int flags;
    int sourceSize;
    int codeLines;
    int commentLines;
    int emptyLines;
    int totalLines;

    public CodeClass(String fullName) {
        this(fullName, new CodePackage(fullName.substring(0, fullName.lastIndexOf('.'))));
    }

    CodeClass(String name, CodePackage pack) {
        this.name = name;
        this.pack = pack;
        usedPackages = new CountSet<>();
        usedClasses = new CountSet<>();
        annotations = new HashSet<>();
        sourceFile = "Unknown";
    }

    public boolean isParsed() {
        return superClass != null;
    }

    public List<MemberInfo> getMembers() {
        final List<MemberInfo> members = new ArrayList<>();
        members.addAll(methods);
        members.addAll(fields);
        return members;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return name.substring(pack.getName().length() + 1);
    }

    public CodePackage getPackage() {
        return pack;
    }

    public String getSuperClass() {
        return superClass;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Set<CodeClass> getAnnotations() {
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

    public boolean isConcrete() {
        return !Modifier.isAbstract(flags) && !Modifier.isInterface(flags);
    }

    public int getSourceSize() {
        return sourceSize;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public int getCommentLines() {
        return commentLines;
    }

    public int getEmptyLines() {
        return emptyLines;
    }

    public int getTotalLines() {
        return totalLines;
    }

    @Override
    public CodeClass self() {
        return this;
    }

    public Collection<CodePackage> usedForeignPackages() {
        final Set<CodePackage> res = new HashSet<>(usedPackages());
        res.remove(pack);
        return res;
    }

    public Collection<CodePackage> usedPackages() {
        return usedPackages.asSet();
    }

    public Map<CodePackage, Integer> usedPackageCounts() {
        return usedPackages.asMap();
    }

    public Collection<CodeClass> usedClasses() {
        return usedClasses.asSet();
    }

    public Map<CodeClass, Integer> usedClassCounts() {
        return usedClasses.asMap();
    }

    public boolean uses(CodePackage pack) {
        return usedPackages.contains(pack);
    }

    @Override
    public Collection<CodeClass> uses() {
        return usedClasses();
    }

    @Override
    public String getPackageName() {
        return pack.getName();
    }

    @Override
    public Collection<String> usedVia(UsingElement<CodeClass> other) {
        return emptyList();
    }

    @Override
    public boolean isMatchedBy(LocationMatcher matcher) {
        return matcher.matchesClass(name);
    }

    void addImport(String type, Model model) {
        if (!name.equals(type)) {
            final CodeClass clazz = model.getOrCreateClass(type);
            if (clazz != null) {
                final String packName = model.packageOf(type);
                final CodePackage p = model.getOrCreatePackage(packName);
                usedPackages.add(p);
                pack.addEfferent(p);
                usedClasses.add(clazz);
            }
        }
    }

    void addAnnotation(String type, Model model, Collection<CodeClass> annotations) {
        final CodeClass clazz = model.getOrCreateClass(type);
        if (clazz != null) {
            addImport(type, model);
            annotations.add(clazz);
        }
    }

    public boolean equals(Object other) {
        if (other instanceof CodeClass) {
            final CodeClass otherClass = (CodeClass) other;
            return otherClass.getName().equals(getName()) && otherClass.getPackage().equals(getPackage());
        }
        return false;
    }

    public int hashCode() {
        return getName().hashCode() * 31 + getPackage().hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
