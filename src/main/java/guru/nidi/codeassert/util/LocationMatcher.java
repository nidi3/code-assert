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
package guru.nidi.codeassert.util;

import java.util.List;

/**
 * Matches a given source code location and name against a predefined list of locations and names.
 */
public class LocationMatcher {
    private final List<String> locs;
    private final List<String> names;

    /**
     * Empty lists match any input.
     *
     * @param locs  The locations to match against.
     *              Has the form [package.][class][#method].
     *              All three elements may start or end with a wildcard *.
     * @param names The names to match against.
     */
    public LocationMatcher(List<String> locs, List<String> names) {
        this.locs = locs;
        this.names = names;
    }

    /**
     * @param name      the name to be matched
     * @param className the class name to be matched
     * @param method    the method to be matched
     * @return If the given name and location (className and method)
     * both match any of the predefined names and locations.
     */
    public boolean matches(String name, String className, String method) {
        if (locs.isEmpty()) {
            return matchesName(name);
        }
        for (final String loc : locs) {
            if (matches(loc, name, className, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String loc, String name, String className, String method) {
        if (!matchesName(name)) {
            return false;
        }
        final int methodPos = loc.indexOf('#');
        if (methodPos < 0) {
            return matchesClass(loc, className);
        }
        if (methodPos == 0) {
            return matchesMethod(loc.substring(1), method);
        }
        return matchesClass(loc.substring(0, methodPos), className) && matchesMethod(loc.substring(methodPos + 1), method);
    }

    private boolean matchesName(String name) {
        return names.isEmpty() || names.contains(name);
    }

    private boolean matchesMethod(String pattern, String name) {
        return wildcardMatch(pattern, name);
    }

    private boolean matchesClass(String pattern, String name) {
        final int namePos = name.lastIndexOf('.');
        final int patternPos = pattern.lastIndexOf('.');
        final boolean matchesPackage = namePos < 0 || patternPos < 0 || wildcardMatch(pattern.substring(0, patternPos), name.substring(0, namePos));
        final boolean matchesClass = wildcardMatch(pattern.substring(patternPos + 1), name.substring(namePos + 1));
        return matchesPackage && matchesClass;
    }

    private boolean wildcardMatch(String pattern, String test) {
        if (pattern.startsWith("*") && pattern.endsWith("*")) {
            return pattern.length() == 1 || test.contains(pattern.substring(1, pattern.length() - 1));
        }
        if (pattern.startsWith("*")) {
            return test.endsWith(pattern.substring(1));
        }
        if (pattern.endsWith("*")) {
            return test.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return test.equals(pattern);
    }

    @Override
    public String toString() {
        return (names.isEmpty() ? "all" : names) + " in " +
                (locs.isEmpty() ? "everywhere" : locs);
    }
}