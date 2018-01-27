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
package guru.nidi.codeassert.config;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class In {
    private final List<Location> locs;

    private In(List<Location> locs) {
        this.locs = locs;
    }

    public static In locs(String... locs) {
        final List<Location> ls = new ArrayList<>();
        for (final String loc : locs) {
            ls.add(Location.of(loc));
        }
        return new In(ls);
    }

    public static In loc(String loc) {
        return new In(singletonList(Location.of(loc)));
    }

    public static In everywhere() {
        return locs();
    }

    public static In languages(Language... languages) {
        return In.everywhere().withLanguages(languages);
    }

    public static In packages(String... packages) {
        return In.everywhere().withPackages(packages);
    }

    public static In classes(String... classes) {
        return In.everywhere().withClasses(classes);
    }

    public static In classes(Class<?>... classes) {
        final List<Location> ls = new ArrayList<>();
        for (final Class<?> loc : classes) {
            ls.add(Location.ofClass(loc));
        }
        return new In(ls);
    }

    public static In clazz(Class<?> clazz) {
        return loc(clazz.getName());
    }

    public static In methods(String... methods) {
        return In.everywhere().withMethods(methods);
    }

    public In and(In in) {
        final List<Location> res = new ArrayList<>(locs);
        res.addAll(in.locs);
        return new In(res);
    }

    public In withLanguages(Language... languages) {
        final List<Location> res = new ArrayList<>();
        for (final Language language : languages) {
            for (final Location loc : baseLocs()) {
                res.add(loc.andLanguage(language));
            }
        }
        return new In(res);
    }

    public In withPackages(String... packages) {
        final List<Location> res = new ArrayList<>();
        for (final String pack : packages) {
            for (final Location loc : baseLocs()) {
                res.add(loc.andPackage(pack));
            }
        }
        return new In(res);
    }

    public In withClasses(String... classes) {
        final List<Location> res = new ArrayList<>();
        for (final String clazz : classes) {
            for (final Location loc : baseLocs()) {
                res.add(loc.andClass(clazz));
            }
        }
        return new In(res);
    }

    public In withMethods(String... methods) {
        final List<Location> res = new ArrayList<>();
        for (final String method : methods) {
            for (final Location loc : baseLocs()) {
                res.add(loc.andMethod(method));
            }
        }
        return new In(res);
    }

    private List<Location> baseLocs() {
        if (locs.isEmpty()) {
            final List<Location> res = new ArrayList<>(locs);
            res.add(Location.all());
            return res;
        }
        return locs;
    }

    public Ignore ignore(String... names) {
        return new Ignore(locs, asList(names));
    }

    public Ignore ignoreAll() {
        return ignore();
    }
}
