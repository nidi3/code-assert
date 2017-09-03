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

import static guru.nidi.codeassert.dependency.MatcherUtils.*;

public class DependencyResultMatcher extends TypeSafeMatcher<DependencyResult> {
    private final boolean nonExisting;
    private final boolean undefined;

    public DependencyResultMatcher(boolean nonExisting, boolean undefined) {
        this.nonExisting = nonExisting;
        this.undefined = undefined;
    }

    @Override
    protected boolean matchesSafely(DependencyResult item) {
        return matches(item.findings());
    }

    protected boolean matches(Dependencies dependencies) {
        return dependencies.getMissing().isEmpty() && dependencies.getDenied().isEmpty()
                && (dependencies.getNotExisting().isEmpty() || !nonExisting)
                && (dependencies.getUndefined().isEmpty() || !undefined);
    }

    public void describeTo(Description description) {
        description.appendText("Comply with rules");
    }

    @Override
    protected void describeMismatchSafely(DependencyResult item, Description description) {
        describeMismatch(item.findings(), description);
    }

    protected void describeMismatch(Dependencies dependencies, Description description) {
        describeNotExisting(dependencies, description);
        describeUndefined(dependencies, description);
        describeMissing(dependencies, description);
        describeForbidden(dependencies, description);
    }

    private void describeForbidden(Dependencies result, Description description) {
        if (!result.getDenied().isEmpty()) {
            description.appendText("\nFound forbidden dependencies:\n");
            for (final String elem : sorted(result.getDenied().getElements())) {
                description.appendText(elem + " ->\n");
                description.appendText(deps("  ", result.getDenied().getDependencies(elem)));
            }
        }
    }

    private void describeMissing(Dependencies result, Description description) {
        if (!result.getMissing().isEmpty()) {
            description.appendText("\nFound missing dependencies:\n");
            for (final String elem : sorted(result.getMissing().getElements())) {
                description.appendText(elem + " ->\n");
                for (final String dep : sorted(result.getMissing().getDependencies(elem).keySet())) {
                    description.appendText("  " + dep + "\n");
                }
            }
        }
    }

    private void describeUndefined(Dependencies result, Description description) {
        if (undefined && !result.getUndefined().isEmpty()) {
            description.appendText("\nFound elements which are not defined:\n");
            description.appendText(join(sorted(result.getUndefined())) + "\n");
        }
    }

    private void describeNotExisting(Dependencies result, Description description) {
        if (nonExisting && !result.getNotExisting().isEmpty()) {
            description.appendText("\nDefined, but not existing elements:\n");
            description.appendText(join(sortedPatterns(result.getNotExisting())) + "\n");
        }
    }
}
