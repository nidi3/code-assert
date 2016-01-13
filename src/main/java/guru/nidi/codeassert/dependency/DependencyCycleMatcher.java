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

import guru.nidi.codeassert.model.JavaClass;
import guru.nidi.codeassert.model.JavaPackage;
import guru.nidi.codeassert.model.ModelResult;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.*;

import static guru.nidi.codeassert.dependency.MatcherUtils.*;

/**
 *
 */
public class DependencyCycleMatcher extends TypeSafeMatcher<ModelResult> {
    private static final Comparator<DependencyMap> DEP_MAP_COMPARATOR = new DependencyMapComparator();

    private final boolean packages;
    private final Set<String>[] exceptions;

    @SafeVarargs
    public DependencyCycleMatcher(boolean packages, Set<String>... exceptions) {
        this.packages = packages;
        this.exceptions = exceptions;
    }

    @Override
    protected boolean matchesSafely(ModelResult item) {
        return result(item).isEmptyExcept(exceptions);
    }

    public void describeTo(Description description) {
        description.appendText("Does not have cycles");
    }

    @Override
    protected void describeMismatchSafely(ModelResult item, Description description) {
        final CycleResult result = result(item);
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

    private CycleResult result(ModelResult item) {
        final Collection<JavaPackage> packs = item.findings().getPackages();
        final Collection<JavaClass> classes = item.findings().getClasses();
        return packages
                ? DependencyRules.analyzeCycles(packs)
                : DependencyRules.analyzeCycles(classes);
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
