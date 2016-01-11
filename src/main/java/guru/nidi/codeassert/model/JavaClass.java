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

/**
 * The <code>JavaClass</code> class represents a Java
 * class or interface.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaClass {
    private String className;
    private String packageName;
    private final Map<String, JavaPackage> imports;
    private String sourceFile;

    public JavaClass(String name) {
        className = name;
        packageName = "default";
        imports = new HashMap<>();
        sourceFile = "Unknown";
    }

    void setName(String name) {
        className = name;
    }

    public String getName() {
        return className;
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
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

    public void addImport(JavaPackage jPackage) {
        if (!jPackage.getName().equals(getPackageName())) {
            imports.put(jPackage.getName(), jPackage);
        }
    }

    public boolean hasImportsMatchedBy(String name) {
        return ! JavaPackage.allMatchesBy(getImports(), name).isEmpty();
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
}
