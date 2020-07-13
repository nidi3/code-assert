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

/**
 * The LocationMatcher is used to match a code location.
 * A pattern has the form [language:][package][[/]class][#method]. <br>
 * package and class are separated by the first occurrence of \.[*+]?[A-Z]
 * If this is not intended or clear, a / can be used to separate package and class. <br>
 * package, class, method may start and/or end with a wildcard '*' or '+'. <br>
 * * means zero or more characters, + means one or more characters,
 * .* means zero characters or . followed by one or more characters.
 */
public class LocationMatcher implements Comparable<LocationMatcher> {
    private final Location loc;

    public LocationMatcher(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("location must not be null");
        }
        this.loc = location;
    }

    public boolean matchesPackage(String packageName) {
        return matchesPattern(loc.pack, packageName)
                && matchesAll(loc.clazz) && matchesAll(loc.method);
    }

    public boolean matchesClass(String className) {
        final int pos = className.lastIndexOf('.');
        return pos < 0
                ? matchesAll(loc.method) && matchesAll(loc.pack) && matchesClassPattern(loc.clazz, className)
                : matchesPackageClass(className.substring(0, pos), className.substring(pos + 1));
    }

    public boolean matchesPackageClass(String packageName, String className) {
        return matchesPattern(loc.pack, packageName)
                && matchesClassPattern(loc.clazz, className) && matchesAll(loc.method);
    }

    public boolean matches(String packageName, String className, String methodName) {
        final boolean matchesClass = matchesAll(loc.method)
                ? matchesClassPattern(loc.clazz, className)
                : matchesPattern(loc.clazz, className);
        return matchesPattern(loc.pack, packageName) && matchesClass && matchesPattern(loc.method, methodName);
    }

    public boolean matchesLanguage(Language language) {
        return language == null || loc.language == null || loc.language == language;
    }

    public int specificity() {
        return specificity(loc.pack) + specificity(loc.clazz) + specificity(loc.method);
    }

    private int specificity(String pattern) {
        if (matchesAll(pattern)) {
            return 1;
        }
        int s = 4;
        if (pattern.startsWith("*") || pattern.startsWith("+")) {
            s--;
        }
        if (pattern.endsWith("*") || pattern.endsWith("+")) {
            s--;
        }
        return s;
    }

    public String getPattern() {
        return loc.getPattern();
    }

    private boolean matchesAll(String pattern) {
        return pattern.length() == 0 || "*".equals(pattern);
    }

    private boolean matchesClassPattern(String pat, String name) {
        if (matchesPattern(pat, name)) {
            return true;
        }
        final int pos = name.indexOf('$');
        if (pos >= 0) {
            return matchesPattern(pat, name.substring(0, pos));
        }
        return false;
    }

    static boolean matchesPattern(String pat, String name) {
        if (pat.length() == 0 || "*".equals(pat) || ("+".equals(pat) && name.length() > 0)) {
            return true;
        }
        if (pat.endsWith(".*")) {
            return matchesPattern(pat.substring(0, pat.length() - 2), name)
                    || matchesPattern(pat.substring(0, pat.length() - 1) + "+", name);
        }
        String pattern = pat;
        final char start = pattern.charAt(0);
        if (start == '*' || start == '+') {
            pattern = pattern.substring(1);
        }
        final char end = pattern.charAt(pattern.length() - 1);
        if (end == '*' || end == '+') {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        return doMatchesPattern(start, end, pattern, name);
    }

    private static boolean doMatchesPattern(char start, char end, String pat, String name) {
        final int pos = name.indexOf(pat);
        if (pos < 0) {
            return false;
        }

        final boolean startsWithPat = pos == 0;
        final boolean endsWithPat = pos + pat.length() == name.length();
        final boolean startOk = start == '*' || ((start == '+') != startsWithPat);
        final boolean endOk = end == '*' || ((end == '+') != endsWithPat);
        return startOk && endOk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LocationMatcher that = (LocationMatcher) o;

        return loc.equals(that.loc);
    }

    @Override
    public int hashCode() {
        return loc.hashCode();
    }

    @Override
    public String toString() {
        return loc.toString();
    }

    @Override
    public int compareTo(LocationMatcher p) {
        return loc.compareTo(p.loc);
    }
}
