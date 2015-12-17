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

import guru.nidi.codeassert.model.Project;
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

    public static Matcher<Project> matchesRules(final DependencyRules rules) {
        return new RuleMatcher(rules, false, false);
    }

    public static Matcher<Project> matchesExactly(final DependencyRules rules) {
        return new RuleMatcher(rules, true, true);
    }

    public static Matcher<Project> matchesIgnoringNonExisting(final DependencyRules rules) {
        return new RuleMatcher(rules, false, true);
    }

    public static Matcher<Project> matchesIgnoringUndefined(final DependencyRules rules) {
        return new RuleMatcher(rules, true, false);
    }

    public static Matcher<Project> hasNoCycles() {
        return new CycleMatcher();
    }

    @SafeVarargs
    public static Matcher<Project> hasNoCyclesExcept(Set<String>... cyclicGroups) {
        return new CycleMatcher(cyclicGroups);
    }

    private static class RuleMatcher extends TypeSafeMatcher<Project> {
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
        protected boolean matchesSafely(Project item) {
            result = rules.analyzeRules(item.getPackages());
            return result.getMissing().isEmpty() && result.getDenied().isEmpty() &&
                    (result.getNotExisting().isEmpty() || !nonExisting) &&
                    (result.getUndefined().isEmpty() || !undefined);
        }

        public void describeTo(Description description) {
            description.appendText("Comply with rules");
        }

        @Override
        protected void describeMismatchSafely(Project item, Description description) {
            if (nonExisting && !result.getNotExisting().isEmpty()) {
                description.appendText("\nDefined, but not existing packages:\n");
                description.appendText(join(sorted(result.getNotExisting())) + "\n");
            }
            if (undefined && !result.getUndefined().isEmpty()) {
                description.appendText("\nFound packages which are not defined:\n");
                description.appendText(join(sorted(result.getUndefined())) + "\n");
            }
            if (!result.getMissing().isEmpty()) {
                description.appendText("\nFound missing dependencies:\n");
                for (String pack : sorted(result.getMissing().getPackages())) {
                    description.appendText(pack + " ->\n");
                    for (final String dep : sorted(result.getMissing().getDependencies(pack).keySet())) {
                        description.appendText("  " + dep + "\n");
                    }
                }
            }
            if (!result.getDenied().isEmpty()) {
                description.appendText("\nFound forbidden dependencies:\n");
                for (String pack : sorted(result.getDenied().getPackages())) {
                    description.appendText(pack + " ->\n");
                    final Map<String, Set<String>> deps = result.getDenied().getDependencies(pack);
                    for (final String dep : sorted(deps.keySet())) {
                        description.appendText("  " + dep + " (by " + join(deps.get(dep)) + ")\n");
                    }
                }
            }
        }
    }

    private static class CycleMatcher extends TypeSafeMatcher<Project> {
        private final Set<String>[] exceptions;
        private CycleResult result;

        @SafeVarargs
        public CycleMatcher(Set<String>... exceptions) {
            this.exceptions = exceptions;
        }

        @Override
        protected boolean matchesSafely(Project item) {
            result = DependencyRules.analyzeCycles(item.getPackages());
            return result.isEmptyExcept(exceptions);
        }

        public void describeTo(Description description) {
            description.appendText("Does not have cycles");
        }

        @Override
        protected void describeMismatchSafely(Project item, Description description) {
            if (!result.isEmptyExcept(exceptions)) {
                description.appendText("Found these cyclic groups:\n");
                for (DependencyMap cycle : result.getCyclesExcept(exceptions)) {
                    description.appendText("\n- Group of " + cycle.getPackages().size() + ": " + join(sorted(cycle.getPackages())) + "\n");
                    for (String pack : sorted(cycle.getPackages())) {
                        description.appendText("  " + pack + " ->\n");
                        final Map<String, Set<String>> deps = cycle.getDependencies(pack);
                        for (final String dep : sorted(deps.keySet())) {
                            description.appendText("    " + dep + " (by " + join(deps.get(dep)) + ")\n");
                        }
                    }
                }
            }
        }
    }

    private static String join(Collection<String> packs) {
        String s = "";
        for (String pack : sorted(packs)) {
            s += ", " + pack;
        }
        return s.length() > 0 ? s.substring(2) : s;
    }

    private static List<String> sorted(Collection<String> ss) {
        final List<String> sorted = new ArrayList<>(ss);
        Collections.sort(sorted);
        return sorted;
    }

}
