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

public class Model {
    final Map<String, JavaPackage> packages = new HashMap<>();
    final Map<String, JavaClass> classes = new HashMap<>();

    JavaPackage getOrCreatePackage(String name) {
        JavaPackage pack = packages.get(name);
        if (pack == null) {
            pack = new JavaPackage(name);
            packages.put(name, pack);
        }
        return pack;
    }

    JavaClass getOrCreateClass(String name) {
        JavaClass clazz = classes.get(name);
        if (clazz == null) {
            final JavaPackage pack = getOrCreatePackage(packageOf(name));
            clazz = new JavaClass(name, pack);
            classes.put(name, clazz);
            pack.addClass(clazz);
        }
        return clazz;
    }

    static String packageOf(String type) {
        final int pos = type.lastIndexOf('.');
        return pos < 0 ? "<No Package>" : type.substring(0, pos);
    }

    @SuppressWarnings("unchecked")
    public <T extends UsingElement<T>> View<T> view(Class<T> type) {
        if (type == JavaPackage.class) {
            return (View<T>) packageView();
        }
        if (type == JavaClass.class) {
            return (View<T>) classView();
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    public View<JavaPackage> packageView() {
        return new View<JavaPackage>() {
            @Override
            public Iterator<JavaPackage> iterator() {
                return packages.values().iterator();
            }
        };
    }

    public View<JavaClass> classView() {
        return new View<JavaClass>() {
            @Override
            public Iterator<JavaClass> iterator() {
                return classes.values().iterator();
            }
        };
    }

    public abstract class View<T extends UsingElement<T>> implements Iterable<T> {
        public List<T> matchingElements(LocationMatcher matcher) {
            final List<T> res = new ArrayList<>();
            for (final T elem : this) {
                if (elem.isMatchedBy(matcher)) {
                    res.add(elem);
                }
            }
            return res;
        }
    }

    public Collection<JavaPackage> getPackages() {
        return packages.values();
    }

    public Collection<JavaClass> getClasses() {
        return classes.values();
    }

}
