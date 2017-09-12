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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static guru.nidi.codeassert.dependency.DependencyCollector.*;
import static guru.nidi.codeassert.dependency.MatcherUtils.*;

public class DependencyResultMatcher extends TypeSafeMatcher<DependencyResult> {
    private static final String SIMPLE_FORMAT = "%-12s %-45s %s%n";
    private static final String ARROW_FORMAT = "%-12s %-45s %s%n";
    private final boolean nonExisting;
    private final boolean undefined;

    public DependencyResultMatcher(boolean nonExisting, boolean undefined) {
        this.nonExisting = nonExisting;
        this.undefined = undefined;
    }

    @Override
    protected boolean matchesSafely(DependencyResult item) {
        final Dependencies dependencies = item.findings();
        return dependencies.getMissing().isEmpty() && dependencies.getDenied().isEmpty()
                && (dependencies.getNotExisting().isEmpty() || !nonExisting)
                && (dependencies.getUndefined().isEmpty() || !undefined);
    }

    public void describeTo(Description description) {
        description.appendText("Comply with rules");
    }

    @Override
    protected void describeMismatchSafely(DependencyResult item, Description description) {
        final Dependencies dependencies = item.findings();
        description.appendText("\n");
        describeNotExisting(dependencies, description);
        describeUndefined(dependencies, description);
        describeMissing(dependencies, description);
        describeForbidden(dependencies, description);
    }

    private void describeForbidden(Dependencies result, Description description) {
        for (final String elem : sorted(result.getDenied().getElements())) {
            description.appendText(String.format(ARROW_FORMAT, DENIED, elem + " ->", "This dependency is forbidden."));
            description.appendText(deps("  ", result.getDenied().getDependencies(elem)));
        }
    }

    private void describeMissing(Dependencies result, Description description) {
        for (final String elem : sorted(result.getMissing().getElements())) {
            description.appendText(String.format(ARROW_FORMAT, MISSING, elem + " ->", "This dependency is missing."));
            for (final String dep : sorted(result.getMissing().getDependencies(elem).keySet())) {
                description.appendText("  " + dep + "\n");
            }
        }
    }

    private void describeUndefined(Dependencies result, Description description) {
        if (undefined) {
            for (final String elem : sorted(result.getUndefined())) {
                description.appendText(String.format(SIMPLE_FORMAT, UNDEFINED, elem,
                        "There is no rule given for this element."));
            }
        }
    }

    private void describeNotExisting(Dependencies result, Description description) {
        if (nonExisting) {
            for (final String elem : sortedPatterns(result.getNotExisting())) {
                description.appendText(String.format(SIMPLE_FORMAT, NOT_EXISTING, elem,
                        "There is a rule for this element, but it has not been found in the code."));
            }
        }
    }
}
