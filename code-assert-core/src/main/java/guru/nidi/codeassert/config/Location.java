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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;

public final class Location implements Comparable<Location> {
    private static final Pattern CLASS_START = Pattern.compile("(^|\\.)[*+]?[A-Z]");

    final Language language;
    final String pack;
    final String clazz;
    final String method;

    private Location(Language language, String pack, String clazz, String method) {
        checkPattern(pack);
        checkPattern(clazz);
        checkPattern(method);
        this.language = language;
        this.pack = pack;
        this.clazz = clazz;
        this.method = method;
    }

    public static Location all() {
        return new Location(null, "", "", "");
    }

    public static Location ofLanguage(Language language) {
        return new Location(language, "", "", "");
    }

    public static Location ofPackage(String pack) {
        checkPattern(pack);
        return new Location(null, pack, "", "");
    }

    public static Location ofClass(Class<?> clazz) {
        return of(clazz.getName());
    }

    public static Location ofClass(String clazz) {
        checkPattern(clazz);
        return new Location(null, "", clazz, "");
    }

    public static Location ofMethod(String method) {
        checkPattern(method);
        return new Location(null, "", "", method);
    }

    public static Location of(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            throw new IllegalArgumentException("Empty pattern");
        }
        final int colon = pattern.indexOf(':');
        return colon < 0
                ? of(null, pattern)
                : of(Language.valueOf(pattern.substring(0, colon).toUpperCase(ENGLISH)), pattern.substring(colon + 1));
    }

    private static Location of(Language language, String rest) {
        final int hash = rest.indexOf('#');
        return hash < 0
                ? of(language, "", rest)
                : of(language, rest.substring(hash + 1), rest.substring(0, hash));
    }

    private static Location of(Language language, String method, String rest) {
        final int slash = rest.indexOf('/');
        if (slash >= 0) {
            return new Location(language, rest.substring(0, slash), rest.substring(slash + 1), method);
        }
        final Matcher matcher = CLASS_START.matcher(rest);
        if (matcher.find()) {
            final String clazz = rest.substring(matcher.start() + matcher.group(1).length());
            return new Location(language, rest.substring(0, matcher.start()), clazz, method);
        }
        return new Location(language, rest, "", method);
    }

    public Location andLanguage(Language language) {
        return new Location(language, pack, clazz, method);
    }

    public Location andPackage(String pack) {
        return new Location(language, pack, clazz, method);
    }

    public Location andClass(String clazz) {
        return new Location(language, pack, clazz, method);
    }

    public Location andMethod(String method) {
        return new Location(language, pack, clazz, method);
    }

    public String getPattern() {
        final boolean noPackEndDot = pack.endsWith("*") || pack.endsWith("+") || pack.length() == 0;
        return (language == null ? "" : (language + ":"))
                + (noPackEndDot || clazz.length() == 0 ? pack : (pack + "."))
                + clazz
                + (method.length() == 0 ? "" : ("#" + method));
    }

    private static void checkPattern(String pattern) {
        if ("**".equals(pattern) || "+*".equals(pattern) || "*+".equals(pattern) || "++".equals(pattern)) {
            throw new IllegalArgumentException("Wildcard " + pattern + " is illegal");
        }
        for (int i = 0; i < pattern.length(); i++) {
            if ((pattern.charAt(i) == '*' || pattern.charAt(i) == '+') && i != 0 && i != pattern.length() - 1) {
                throw new IllegalArgumentException("Wildcards must be at begin or end of a pattern");
            }
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

        final Location location = (Location) o;

        if (language != location.language) {
            return false;
        }
        if (!pack.equals(location.pack)) {
            return false;
        }
        if (!clazz.equals(location.clazz)) {
            return false;
        }
        return method.equals(location.method);
    }

    @Override
    public int hashCode() {
        int result = language != null ? language.hashCode() : 0;
        result = 31 * result + pack.hashCode();
        result = 31 * result + clazz.hashCode();
        result = 31 * result + method.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getPattern();
    }

    @Override
    public int compareTo(Location o) {
        return getPattern().compareTo(o.getPattern());
    }
}
