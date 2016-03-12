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

import guru.nidi.codeassert.config.LocationMatcher;

import java.util.*;

/**
 * The <code>JavaPackage</code> class represents a Java package.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class JavaPackage extends UsingElement<JavaPackage> {
    private final String name;
    private final Set<JavaClass> classes;
    private final List<JavaPackage> uses;

    JavaPackage(String name) {
        this.name = name;
        classes = new HashSet<>();
        uses = new ArrayList<>();
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

    void addEfferent(JavaPackage jPackage) {
        if (!jPackage.getName().equals(getName()) && !uses.contains(jPackage)) {
            uses.add(jPackage);
        }
    }

    public Set<String> usedVia(UsingElement<JavaPackage> to) {
        final Set<String> res = new HashSet<>();
        for (final JavaClass jc : getClasses()) {
            if (jc.uses(to.self())) {
                res.add(jc.getName());
            }
        }
        return res;
    }

    @Override
    public boolean isMatchedBy(LocationMatcher matcher) {
        return matcher.matchesPackage(name);
    }

    @Override
    public JavaPackage self() {
        return this;
    }

    @Override
    public Collection<JavaPackage> uses() {
        return uses;
    }

    public boolean equals(Object other) {
        if (other instanceof JavaPackage) {
            final JavaPackage otherPackage = (JavaPackage) other;
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
