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

import guru.nidi.codeassert.config.LocationMatcher;

import java.util.*;

final class MatcherUtils {
    private MatcherUtils() {
    }

    public static String deps(String prefix, Map<String, DependencyMap.Info> deps) {
        final StringBuilder s = new StringBuilder();
        for (final String dep : sorted(deps.keySet())) {
            s.append(prefix).append(dep);
            final Set<String> by = deps.get(dep).getVias();
            if (!by.isEmpty()) {
                s.append(" (by ").append(join(by)).append(')');
            }
            s.append('\n');
        }
        return s.toString();
    }

    public static String join(Collection<String> packs) {
        final StringBuilder s = new StringBuilder();
        for (final String pack : sorted(packs)) {
            s.append(", ").append(pack);
        }
        return s.length() > 0 ? s.substring(2) : s.toString();
    }

    public static List<String> sorted(Collection<String> ss) {
        final List<String> sorted = new ArrayList<>(ss);
        Collections.sort(sorted);
        return sorted;
    }

    public static List<String> sortedPatterns(Collection<LocationMatcher> patterns) {
        final List<String> ss = new ArrayList<>();
        for (final LocationMatcher pattern : patterns) {
            ss.add(pattern.toString());
        }
        return sorted(ss);
    }
}
