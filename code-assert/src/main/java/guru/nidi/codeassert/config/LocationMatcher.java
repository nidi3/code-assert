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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The LocationMatcher is used to match a code location.
 * A pattern has the form [package][[/]class][#method].
 * package and class are separated by the first occurrence of \.\*?[A-Z]
 * If this is not intended or clear, a / can be used to separate package and class.
 * All three elements may start and/or end with a wildcard *.
 */
public class LocationMatcher implements Comparable<LocationMatcher> {
    private static final Pattern CLASS_START = Pattern.compile("(^|\\.)\\*?[A-Z]");
    private final String pattern;
    private final String packagePat;
    private final String classPat;
    private final String methodPat;

    public LocationMatcher(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            throw new IllegalArgumentException("Empty pattern");
        }
        this.pattern = pattern;
        final int hash = pattern.indexOf('#');
        methodPat = hash < 0 ? "" : pattern.substring(hash + 1);
        final String qualif = hash < 0 ? pattern : pattern.substring(0, hash);
        final int slash = qualif.indexOf('/');
        if (slash >= 0) {
            packagePat = qualif.substring(0, slash);
            classPat = qualif.substring(slash + 1);
        } else {
            final Matcher matcher = CLASS_START.matcher(qualif);
            if (matcher.find()) {
                packagePat = qualif.substring(0, matcher.start());
                classPat = qualif.substring(matcher.start() + matcher.group(1).length());
            } else {
                packagePat = qualif;
                classPat = "";
            }
        }
        checkPattern(packagePat);
        checkPattern(classPat);
        checkPattern(methodPat);
    }

    public boolean matchesPackage(String packageName) {
        return matchesPattern(packagePat, packageName)
                && matchesAll(classPat) && matchesAll(methodPat);
    }

    public boolean matchesClass(String className) {
        final int pos = className.lastIndexOf('.');
        return pos < 0
                ? matchesAll(methodPat) && matchesAll(packagePat) && matchesClassPattern(classPat, className)
                : matchesPackageClass(className.substring(0, pos), className.substring(pos + 1));
    }

    public boolean matchesPackageClass(String packageName, String className) {
        return matchesPattern(packagePat, packageName) && matchesClassPattern(classPat, className) && matchesAll(methodPat);
    }

    public boolean matches(String packageName, String className, String methodName) {
        final boolean matchesClass = matchesAll(methodPat)
                ? matchesClassPattern(classPat, className)
                : matchesPattern(classPat, className);
        return matchesPattern(packagePat, packageName) && matchesClass && matchesPattern(methodPat, methodName);
    }

    public int specificity() {
        return specificity(packagePat) + specificity(classPat) + specificity(methodPat);
    }

    private int specificity(String pattern) {
        if (matchesAll(pattern)) {
            return 1;
        }
        int s = 4;
        if (pattern.startsWith("*")) {
            s--;
        }
        if (pattern.endsWith("*")) {
            s--;
        }
        return s;
    }

    public String getPattern() {
        return pattern;
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
        if (pat.length() == 0) {
            return true;
        }
        if (pat.startsWith("*") && pat.endsWith("*")) {
            return pat.length() == 1 || name.contains(pat.substring(1, pat.length() - 1));
        }
        if (pat.startsWith("*")) {
            return name.endsWith(pat.substring(1));
        }
        if (pat.endsWith("*")) {
            return name.startsWith(pat.substring(0, pat.length() - 1));
        }
        return name.equals(pat);
    }

    private void checkPattern(String pattern) {
        if ("**".equals(pattern)) {
            throw new IllegalArgumentException("Wildcard ** is illegal");
        }
        checkPattern(pattern, pattern.indexOf('*'));
        checkPattern(pattern, pattern.lastIndexOf('*'));
    }

    private void checkPattern(String pattern, int pos) {
        if (pos > 0 && pos != pattern.length() - 1) {
            throw new IllegalArgumentException("Wildcard * must be at begin or end of pattern");
        }
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

        return pattern.equals(that.pattern);

    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public int compareTo(LocationMatcher p) {
        return pattern.compareTo(p.pattern);
    }
}
