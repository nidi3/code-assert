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
 * The <code>JavaPackage</code> class represents a Java package.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaPackage {
    private final String name;
    private final Set<JavaClass> classes;
    private final List<JavaPackage> afferents;
    private final List<JavaPackage> efferents;

    public JavaPackage(String name) {
        this.name = name;
        classes = new HashSet<>();
        afferents = new ArrayList<>();
        efferents = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addClass(JavaClass clazz) {
        classes.add(clazz);
    }

    public Collection<JavaClass> getClasses() {
        return classes;
    }

    /**
     * Adds the specified Java package as an efferent of this package
     * and adds this package as an afferent of it.
     *
     * @param imported Java package.
     */
    public void dependsUpon(JavaPackage... imported) {
        for (final JavaPackage pack : imported) {
            addEfferent(pack);
            pack.addAfferent(this);
        }
    }

    /**
     * Adds the specified Java package as an afferent of this package.
     *
     * @param jPackage Java package.
     */
    private void addAfferent(JavaPackage jPackage) {
        if (!jPackage.getName().equals(getName())) {
            if (!afferents.contains(jPackage)) {
                afferents.add(jPackage);
            }
        }
    }

    public Collection<JavaPackage> getAfferents() {
        return afferents;
    }

    private void addEfferent(JavaPackage jPackage) {
        if (!jPackage.getName().equals(getName())) {
            if (!efferents.contains(jPackage)) {
                efferents.add(jPackage);
            }
        }
    }

    public Collection<JavaPackage> getEfferents() {
        return efferents;
    }

    public boolean isMatchedBy(String name) {
        return name.endsWith(".*")
                ? getName().startsWith(name.substring(0, name.length() - 1))
                : getName().equals(name);
    }

    public boolean isMatchedByAny(List<String> names) {
        for (String name : names) {
            if (isMatchedBy(name)) {
                return true;
            }
        }
        return false;
    }

    public static List<JavaPackage> allMatchesBy(Collection<JavaPackage> packages, String name) {
        final List<JavaPackage> res = new ArrayList<>();
        for (JavaPackage pack : packages) {
            if (pack.isMatchedBy(name)) {
                res.add(pack);
            }
        }
        return res;
    }

    public boolean hasEfferentsMatchedBy(String name) {
        return !allMatchesBy(getEfferents(), name).isEmpty();
    }

    public Set<String> classesWithImportsFrom(JavaPackage to) {
        final Set<String> res = new HashSet<>();
        for (JavaClass jc : getClasses()) {
            if (jc.hasImportsMatchedBy(to.getName())) {
                res.add(jc.getName());
            }
        }
        return res;
    }

    public boolean equals(Object other) {
        if (other instanceof JavaPackage) {
            JavaPackage otherPackage = (JavaPackage) other;
            return otherPackage.getName().equals(getName());
        }
        return false;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public String toString() {
        return name;
    }
}
