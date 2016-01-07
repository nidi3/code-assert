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
package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.model.ModelAnalyzer;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.*;

/**
 *
 */
public class DependencyMatchers {
    private DependencyMatchers() {
    }

    public static Matcher<ModelAnalyzer> matchesRules(final DependencyRules rules) {
        return new RuleMatcher(rules, false, false);
    }

    public static Matcher<ModelAnalyzer> matchesExactly(final DependencyRules rules) {
        return new RuleMatcher(rules, true, true);
    }

    public static Matcher<ModelAnalyzer> matchesIgnoringNonExisting(final DependencyRules rules) {
        return new RuleMatcher(rules, false, true);
    }

    public static Matcher<ModelAnalyzer> matchesIgnoringUndefined(final DependencyRules rules) {
        return new RuleMatcher(rules, true, false);
    }

    public static Matcher<ModelAnalyzer> hasNoCycles() {
        return new CycleMatcher();
    }

    @SafeVarargs
    public static Matcher<ModelAnalyzer> hasNoCyclesExcept(Set<String>... cyclicGroups) {
        return new CycleMatcher(cyclicGroups);
    }

    private static class RuleMatcher extends TypeSafeMatcher<ModelAnalyzer> {
        private final DependencyRules rules;
        private final boolean nonExisting;
        private final boolean undefined;
        private RuleResult result;

        public RuleMatcher(DependencyRules rules, boolean nonExisting, boolean undefined) {
            this.rules = rules;
            this.nonExisting = nonExisting;
            this.undefined = undefined;
        }

        @Override
        protected boolean matchesSafely(ModelAnalyzer item) {
            result = rules.analyzeRules(item.analyze());
            return result.getMissing().isEmpty() && result.getDenied().isEmpty() &&
                    (result.getNotExisting().isEmpty() || !nonExisting) &&
                    (result.getUndefined().isEmpty() || !undefined);
        }

        public void describeTo(Description description) {
            description.appendText("Comply with rules");
        }

        @Override
        protected void describeMismatchSafely(ModelAnalyzer item, Description description) {
            describeNotExisting(description);
            describeUndefined(description);
            describeMissing(description);
            describeForbidden(description);
        }

        private void describeForbidden(Description description) {
            if (!result.getDenied().isEmpty()) {
                description.appendText("\nFound forbidden dependencies:\n");
                for (final String pack : sorted(result.getDenied().getPackages())) {
                    description.appendText(pack + " ->\n");
                    description.appendText(deps("  ", result.getDenied().getDependencies(pack)));
                }
            }
        }

        private void describeMissing(Description description) {
            if (!result.getMissing().isEmpty()) {
                description.appendText("\nFound missing dependencies:\n");
                for (final String pack : sorted(result.getMissing().getPackages())) {
                    description.appendText(pack + " ->\n");
                    for (final String dep : sorted(result.getMissing().getDependencies(pack).keySet())) {
                        description.appendText("  " + dep + "\n");
                    }
                }
            }
        }

        private void describeUndefined(Description description) {
            if (undefined && !result.getUndefined().isEmpty()) {
                description.appendText("\nFound packages which are not defined:\n");
                description.appendText(join(sorted(result.getUndefined())) + "\n");
            }
        }

        private void describeNotExisting(Description description) {
            if (nonExisting && !result.getNotExisting().isEmpty()) {
                description.appendText("\nDefined, but not existing packages:\n");
                description.appendText(join(sorted(result.getNotExisting())) + "\n");
            }
        }
    }

    private static class CycleMatcher extends TypeSafeMatcher<ModelAnalyzer> {
        private static final Comparator<DependencyMap> DEP_MAP_COMPARATOR = new DependencyMapComparator();
        private final Set<String>[] exceptions;
        private CycleResult result;

        @SafeVarargs
        public CycleMatcher(Set<String>... exceptions) {
            this.exceptions = exceptions;
        }

        @Override
        protected boolean matchesSafely(ModelAnalyzer item) {
            result = DependencyRules.analyzeCycles(item.analyze());
            return result.isEmptyExcept(exceptions);
        }

        public void describeTo(Description description) {
            description.appendText("Does not have cycles");
        }

        @Override
        protected void describeMismatchSafely(ModelAnalyzer item, Description description) {
            if (!result.isEmptyExcept(exceptions)) {
                description.appendText("Found these cyclic groups:\n");
                for (final DependencyMap cycle : sortedDepMaps(result.getCyclesExcept(exceptions))) {
                    description.appendText("\n- Group of " + cycle.getPackages().size() + ": " + join(sorted(cycle.getPackages())) + "\n");
                    for (final String pack : sorted(cycle.getPackages())) {
                        description.appendText("  " + pack + " ->\n");
                        description.appendText(deps("    ", cycle.getDependencies(pack)));
                    }
                }
            }
        }

        private static List<DependencyMap> sortedDepMaps(Collection<DependencyMap> maps) {
            final List<DependencyMap> sorted = new ArrayList<>(maps);
            Collections.sort(sorted, DEP_MAP_COMPARATOR);
            return sorted;
        }

        private static class DependencyMapComparator implements Comparator<DependencyMap> {
            @Override
            public int compare(DependencyMap d1, DependencyMap d2) {
                final Iterator<String> i1 = sorted(d1.getPackages()).iterator();
                final Iterator<String> i2 = sorted(d2.getPackages()).iterator();
                while (i1.hasNext() && i2.hasNext()) {
                    final String s1 = i1.next();
                    final String s2 = i2.next();
                    final int c = s1.compareTo(s2);
                    if (c != 0) {
                        return c;
                    }
                }
                if (i1.hasNext()) {
                    return 1;
                }
                if (i2.hasNext()) {
                    return -1;
                }
                return 0;
            }
        }
    }

    private static String deps(String prefix, Map<String, Set<String>> deps) {
        final StringBuilder s = new StringBuilder();
        for (final String dep : sorted(deps.keySet())) {
            s.append(prefix).append(dep).append(" (by ").append(join(deps.get(dep))).append(")\n");
        }
        return s.toString();
    }

    private static String join(Collection<String> packs) {
        final StringBuilder s = new StringBuilder();
        for (final String pack : sorted(packs)) {
            s.append(", ").append(pack);
        }
        return s.length() > 0 ? s.substring(2) : s.toString();
    }

    private static List<String> sorted(Collection<String> ss) {
        final List<String> sorted = new ArrayList<>(ss);
        Collections.sort(sorted);
        return sorted;
    }
}
