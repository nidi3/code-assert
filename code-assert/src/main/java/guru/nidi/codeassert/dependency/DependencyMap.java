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
package guru.nidi.codeassert.dependency;


import guru.nidi.codeassert.model.UsingElement;

import java.util.*;
import java.util.Map.Entry;

class DependencyMap {
    private final Map<String, Map<String, Info>> map = new LinkedHashMap<>();

    public <T> void with(int specificity, UsingElement<T> from, UsingElement<T> to) {
        with(specificity, from.getName(), from.usedVia(to), to.getName());
    }

    DependencyMap with(int specificity, String from, Collection<String> vias, String to) {
        Map<String, Info> deps = map.get(from);
        if (deps == null) {
            deps = new HashMap<>();
            map.put(from, deps);
        }
        final Info info = deps.get(to);
        if (info == null) {
            deps.put(to, new Info(vias, specificity));
        } else {
            //TODO specificity?
            info.getVias().addAll(vias);
        }
        return this;
    }

    DependencyMap with(String from, DependencyMap other) {
        final Map<String, Info> infos = other.getDependencies(from);
        for (final Entry<String, Info> entry : infos.entrySet()) {
            final Info info = entry.getValue();
            with(info.getSpecificity(), from, info.getVias(), entry.getKey());
        }
        return this;
    }

    public DependencyMap without(int specificity, String from, String to) {
        final Map<String, Info> deps = map.get(from);
        if (deps != null) {
            final Info info = deps.get(to);
            if (info != null && specificity > info.specificity) {
                deps.remove(to);
                if (deps.isEmpty()) {
                    map.remove(from);
                }
            }
        }
        return this;
    }

    public DependencyMap without(DependencyMap other) {
        for (final Entry<String, Map<String, Info>> entry : other.map.entrySet()) {
            for (final Entry<String, Info> to : entry.getValue().entrySet()) {
                without(to.getValue().specificity, entry.getKey(), to.getKey());
            }
        }
        return this;
    }

    public void merge(DependencyMap deps) {
        for (final Entry<String, Map<String, Info>> entry : deps.map.entrySet()) {
            final Map<String, Info> ds = map.get(entry.getKey());
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

    public Set<String> getElements() {
        return map.keySet();
    }

    /**
     * @param name the name of the element
     * @return A map with all dependencies of a given package.
     * Key: package, Value: A set of all classes importing the package
     */
    public Map<String, Info> getDependencies(String name) {
        return map.get(name);
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

    public static class Info {
        private final Set<String> vias;
        private final int specificity;

        Info(Collection<String> vias, int specificity) {
            this.vias = new HashSet<>(vias);
            this.specificity = specificity;
        }

        public Set<String> getVias() {
            return vias;
        }

        public int getSpecificity() {
            return specificity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Info info = (Info) o;
            return vias.equals(info.vias);
        }

        @Override
        public int hashCode() {
            return vias.hashCode();
        }

        @Override
        public String toString() {
            return vias.toString();
        }
    }
}
