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
package guru.nidi.codeassert.dependency;


import guru.nidi.codeassert.model.JavaPackage;

import java.util.*;

/**
 *
 */
public class DependencyMap {
    private final Map<String, Map<String, Set<String>>> map = new LinkedHashMap<>();

    public void with(JavaPackage from, JavaPackage to) {
        with(from.getName(), from.classesWithImportsFrom(to), to.getName());
    }

    public DependencyMap with(String from, Set<String> fromClasses, String to) {
        Map<String, Set<String>> deps = map.get(from);
        if (deps == null) {
            deps = new HashMap<>();
            map.put(from, deps);
        }
        final Set<String> existingFromClasses = deps.get(to);
        if (existingFromClasses == null) {
            deps.put(to, new HashSet<>(fromClasses));
        } else {
            existingFromClasses.addAll(fromClasses);
        }
        return this;
    }

    public DependencyMap without(String from, String to) {
        final Map<String, Set<String>> deps = map.get(from);
        if (deps != null) {
            deps.remove(to);
            if (deps.isEmpty()) {
                map.remove(from);
            }
        }
        return this;
    }

    public DependencyMap without(DependencyMap other) {
        for (final Map.Entry<String, Map<String, Set<String>>> entry : other.map.entrySet()) {
            for (final String to : entry.getValue().keySet()) {
                without(entry.getKey(), to);
            }
        }
        return this;
    }

    public void merge(DependencyMap deps) {
        for (final Map.Entry<String, Map<String, Set<String>>> entry : deps.map.entrySet()) {
            final Map<String, Set<String>> ds = map.get(entry.getKey());
            if (ds == null) {
                map.put(entry.getKey(), entry.getValue());
            } else {
                ds.putAll(entry.getValue());
            }
        }
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        map.clear();
    }

    public Set<String> getPackages() {
        return map.keySet();
    }

    /**
     * @param pack the package name
     * @return A map with all dependencies of a given package.
     * Key: package, Value: A set of all classes importing the package
     */
    public Map<String, Set<String>> getDependencies(String pack) {
        return map.get(pack);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DependencyMap that = (DependencyMap) o;

        return map.equals(that.map);

    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return map.toString();
    }

}
