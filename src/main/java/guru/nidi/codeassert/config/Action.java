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
package guru.nidi.codeassert.config;

import guru.nidi.codeassert.util.LocationMatcher;

import java.util.List;

/**
 * Actions are created using the In class.
 *
 * @see In
 */
public class Action {
    private final LocationMatcher matcher;

    protected Action(List<String> locs, List<String> names) {
        matcher = new LocationMatcher(locs, names);
    }

    public static In in(final Class<?>... classes) {
        return In.classes(classes);
    }

    public static In in(final String... locs) {
        return In.locs(locs);
    }

    public static In everywhere() {
        return In.everywhere();
    }

    public boolean accept(String name, String className, String method, boolean strictNameMatch) {
        return matcher.matches(name, className, method, strictNameMatch);
    }

    @Override
    public String toString() {
        return matcher.toString();
    }
}
