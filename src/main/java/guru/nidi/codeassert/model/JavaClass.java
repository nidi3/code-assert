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

import java.util.*;

/**
 * The <code>JavaClass</code> class represents a Java
 * class or interface.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaClass {
    private final String className;
    private final JavaPackage pack;
    private final Map<String, JavaPackage> imports;
    private final Set<JavaClass> importClasses;
    private String sourceFile;

    public JavaClass(String name, JavaPackage pack) {
        className = name;
        this.pack = pack;
        imports = new HashMap<>();
        importClasses = new HashSet<>();
        sourceFile = "Unknown";
    }

    public String getName() {
        return className;
    }

    public JavaPackage getPackage() {
        return pack;
    }

    public void setSourceFile(String name) {
        sourceFile = name;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Collection<JavaPackage> getImports() {
        return imports.values();
    }

    public void addImport(String type, JavaClassBuilder builder) {
        final String packName = packageOf(type);
        if (!packName.equals(pack.getName())) {
            final JavaPackage p = builder.getPackage(packName);
            imports.put(packName, p);
            pack.addEfferent(p);
            importClasses.add(builder.getClass(type));
        }
    }

    static String packageOf(String type) {
        final int pos = type.lastIndexOf('.');
        return pos > 0 ? type.substring(0, pos) : "Default";
    }

    public boolean hasImportsMatchedBy(String name) {
        return !JavaPackage.allMatchesBy(getImports(), name).isEmpty();
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
        return className;
    }
}
