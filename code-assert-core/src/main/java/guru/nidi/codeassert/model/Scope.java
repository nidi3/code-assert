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

import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.LocationMatcher;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class Scope<T extends UsingElement<T>> implements Iterable<T> {
    public static final Packages PACKAGES = new Packages(null);
    public static final Classes CLASSES = new Classes(null);

    protected final Model model;

    protected Scope(Model model) {
        this.model = model;
    }

    public static Scope<CodePackage> packages(final Model model) {
        return new Packages(model);
    }

    public static Scope<CodeClass> classes(final Model model) {
        return new Classes(model);
    }

    public Scope<T> in(Model model) {
        try {
            final Constructor<? extends Scope> c = getClass().getDeclaredConstructor(Model.class);
            return c.newInstance(model);
        } catch (ReflectiveOperationException e) {
            throw new AnalyzerException("Could not create new Scope", e);
        }
    }

    public List<T> matchingElements(LocationMatcher matcher) {
        final List<T> res = new ArrayList<>();
        for (final T elem : this) {
            if (elem.isMatchedBy(matcher)) {
                res.add(elem);
            }
        }
        return res;
    }

    public static class Packages extends Scope<CodePackage> {
        protected Packages(Model model) {
            super(model);
        }

        @Override
        public Iterator<CodePackage> iterator() {
            return model.packages.values().iterator();
        }
    }

    public static class Classes extends Scope<CodeClass> {
        public Classes(Model model) {
            super(model);
        }

        @Override
        public Iterator<CodeClass> iterator() {
            return model.classes.values().iterator();
        }
    }

}
