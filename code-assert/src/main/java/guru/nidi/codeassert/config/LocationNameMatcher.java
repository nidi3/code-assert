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

/**
 * Matches a given source code location and name against a predefined list of locations and names.
 */
public class LocationNameMatcher {
    private final List<LocationMatcher> matchers;
    private final List<String> names;

    /**
     * Empty lists match any input.
     *
     * @param locs  The locations to match against.
     *              Has the form [language:][package][[/]class][#method].
     *              package and class are separated by the first uppercase letter.
     *              If this is not intended, a / can be used to separate package and class.
     *              All three elements may start and/or end with a wildcard *.
     * @param names The names to match against.
     */
    public LocationNameMatcher(List<Location> locs, List<String> names) {
        this.matchers = new ArrayList<>();
        for (final Location loc : locs) {
            this.matchers.add(new LocationMatcher(loc));
        }
        this.names = names;
    }

    /**
     * @param name            the name to be matched
     * @param lang            the language to be matched
     * @param className       the class name to be matched
     * @param methodName      the method to be matched
     * @param strictNameMatch if the name must match exactly (as opposed to 'contains')
     * @return If name and location (className and method) both match any of the predefined names and locations.
     */
    public boolean matches(String name, Language lang, String className, String methodName, boolean strictNameMatch) {
        if (matchers.isEmpty()) {
            return matchesName(name, strictNameMatch);
        }
        for (final LocationMatcher matcher : matchers) {
            if (matches(matcher, name, lang, className, methodName, strictNameMatch)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(LocationMatcher matcher, String name, Language language, String className,
                            String methodName, boolean strictNameMatch) {
        if (!matchesName(name, strictNameMatch) || !matcher.matchesLanguage(language)) {
            return false;
        }
        final int pos = className.lastIndexOf('.');
        return pos < 0
                ? matcher.matches("", className, methodName)
                : matcher.matches(className.substring(0, pos), className.substring(pos + 1), methodName);
    }

    private boolean matchesName(String name, boolean strictNameMatch) {
        if (names.isEmpty()) {
            return true;
        }
        for (final String n : names) {
            if (LocationMatcher.matchesPattern(createPattern(n, strictNameMatch), name)) {
                return true;
            }
        }
        return false;
    }

    private String createPattern(String pattern, boolean strictPattern) {
        if (strictPattern) {
            return pattern;
        }
        String wildcarded = pattern;
        if (!pattern.startsWith("*")) {
            wildcarded = "*" + wildcarded;
        }
        if (!pattern.endsWith("*")) {
            wildcarded = wildcarded + "*";
        }
        return wildcarded;
    }

    @Override
    public String toString() {
        return (names.isEmpty() ? "all" : names) + " in " + (matchers.isEmpty() ? "everywhere" : matchers);
    }
}
