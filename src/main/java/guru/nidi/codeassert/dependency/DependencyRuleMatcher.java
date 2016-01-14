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

import static guru.nidi.codeassert.dependency.MatcherUtils.*;

/**
 *
 */
public class DependencyRuleMatcher extends TypeSafeMatcher<ModelResult> {
    private final DependencyRules rules;
    private final boolean nonExisting;
    private final boolean undefined;

    public DependencyRuleMatcher(DependencyRules rules, boolean nonExisting, boolean undefined) {
        this.rules = rules;
        this.nonExisting = nonExisting;
        this.undefined = undefined;
    }

    @Override
    protected boolean matchesSafely(ModelResult item) {
        final RuleResult result = rules.analyzeRules(item.findings());
        return result.getMissing().isEmpty() && result.getDenied().isEmpty() &&
                (result.getNotExisting().isEmpty() || !nonExisting) &&
                (result.getUndefined().isEmpty() || !undefined);
    }

    public void describeTo(Description description) {
        description.appendText("Comply with rules");
    }

    @Override
    protected void describeMismatchSafely(ModelResult item, Description description) {
        final RuleResult result = rules.analyzeRules(item.findings());
        describeNotExisting(result, description);
        describeUndefined(result, description);
        describeMissing(result, description);
        describeForbidden(result, description);
    }

    private void describeForbidden(RuleResult result, Description description) {
        if (!result.getDenied().isEmpty()) {
            description.appendText("\nFound forbidden dependencies:\n");
            for (final String pack : sorted(result.getDenied().getPackages())) {
                description.appendText(pack + " ->\n");
                description.appendText(deps("  ", result.getDenied().getDependencies(pack)));
            }
        }
    }

    private void describeMissing(RuleResult result, Description description) {
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

    private void describeUndefined(RuleResult result, Description description) {
        if (undefined && !result.getUndefined().isEmpty()) {
            description.appendText("\nFound packages which are not defined:\n");
            description.appendText(join(sorted(result.getUndefined())) + "\n");
        }
    }

    private void describeNotExisting(RuleResult result, Description description) {
        if (nonExisting && !result.getNotExisting().isEmpty()) {
            description.appendText("\nDefined, but not existing packages:\n");
            description.appendText(join(sortedPatterns(result.getNotExisting())) + "\n");
        }
    }
}
