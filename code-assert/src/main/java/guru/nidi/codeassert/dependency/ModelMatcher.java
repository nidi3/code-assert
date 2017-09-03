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

import guru.nidi.codeassert.model.Model;
import guru.nidi.codeassert.model.Scope;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ModelMatcher extends TypeSafeMatcher<Model> {
    private final DependencyResultMatcher delegate;
    private final Scope scope;
    private final DependencyRules rules;

    public ModelMatcher(Scope scope, DependencyRules rules, boolean nonExisting, boolean undefined) {
        this.scope = scope;
        this.rules = rules;
        delegate = new DependencyResultMatcher(nonExisting, undefined);
    }

    @Override
    protected boolean matchesSafely(Model model) {
        return delegate.matches(result(model));
    }

    public void describeTo(Description description) {
        delegate.describeTo(description);
    }

    @Override
    protected void describeMismatchSafely(Model model, Description description) {
        delegate.describeMismatch(result(model), description);
    }

    private Dependencies result(Model model) {
        return rules.analyzeRules(scope.in(model));
    }
}
