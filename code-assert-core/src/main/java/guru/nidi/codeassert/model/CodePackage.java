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

import java.util.*;

/**
 * The <code>JavaPackage</code> class represents a Java package.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class CodePackage extends UsingElement<CodePackage> {
    private final String name;
    private final Set<CodeClass> classes;
    private final List<CodePackage> uses;
    private final Set<CodeClass> annotations;

    CodePackage(String name) {
        this.name = name;
        classes = new HashSet<>();
        uses = new ArrayList<>();
        annotations = new HashSet<>();
    }

    @Override
    public String getName() {
        return name;
    }

    public void addClass(CodeClass clazz) {
        classes.add(clazz);
    }

    public void addAnnotation(CodeClass clazz) {
        annotations.add(clazz);
    }

    public Collection<CodeClass> getClasses() {
        return classes;
    }

    public Collection<CodeClass> getAnnotations() {
        return annotations;
    }

    void addEfferent(CodePackage jPackage) {
        if (!jPackage.getName().equals(getName()) && !uses.contains(jPackage)) {
            uses.add(jPackage);
        }
    }

    @Override
    public Set<String> usedVia(UsingElement<CodePackage> to) {
        final Set<String> res = new HashSet<>();
        for (final CodeClass jc : getClasses()) {
            if (jc.uses(to.self())) {
                res.add(jc.getName());
            }
        }
        return res;
    }

    @Override
    public String getPackageName() {
        return name;
    }

    @Override
    public boolean isMatchedBy(LocationMatcher matcher) {
        return matcher.matchesPackage(name);
    }

    @Override
    public CodePackage self() {
        return this;
    }

    @Override
    public Collection<CodePackage> uses() {
        return uses;
    }

    public boolean equals(Object other) {
        if (other instanceof CodePackage) {
            final CodePackage otherPackage = (CodePackage) other;
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
