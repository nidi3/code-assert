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

import guru.nidi.codeassert.model.ModelResult;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.*;

import static guru.nidi.codeassert.dependency.MatcherUtils.deps;
import static guru.nidi.codeassert.dependency.MatcherUtils.join;
import static guru.nidi.codeassert.dependency.MatcherUtils.sorted;

/**
 *
 */
public class DependencyCycleMatcher extends TypeSafeMatcher<ModelResult> {
    private static final Comparator<DependencyMap> DEP_MAP_COMPARATOR = new DependencyMapComparator();
    private final Set<String>[] exceptions;

    @SafeVarargs
    public DependencyCycleMatcher(Set<String>... exceptions) {
        this.exceptions = exceptions;
    }

    @Override
    protected boolean matchesSafely(ModelResult item) {
        final CycleResult result = DependencyRules.analyzeCycles(item.findings());
        return result.isEmptyExcept(exceptions);
    }

    public void describeTo(Description description) {
        description.appendText("Does not have cycles");
    }

    @Override
    protected void describeMismatchSafely(ModelResult item, Description description) {
        final CycleResult result = DependencyRules.analyzeCycles(item.findings());
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
