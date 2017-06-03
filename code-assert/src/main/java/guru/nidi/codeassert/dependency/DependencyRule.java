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
import guru.nidi.codeassert.model.Model;
import guru.nidi.codeassert.model.UsingElement;

import java.util.List;

import static guru.nidi.codeassert.dependency.RuleAccessor.*;

public class DependencyRule extends JavaElement {
    final Usage use = new Usage();
    final Usage usedBy = new Usage();

    DependencyRule(String pattern, boolean allowAll) {
        super(pattern, allowAll);
    }

    public static DependencyRule allowAll(String name) {
        return new DependencyRule(name, true);
    }

    public static DependencyRule denyAll(String name) {
        return new DependencyRule(name, false);
    }

    public DependencyRule mustUse(JavaElement... rules) {
        use.must(rules);
        return this;
    }

    public DependencyRule mayUse(JavaElement... rules) {
        use.may(rules);
        return this;
    }

    public DependencyRule mustNotUse(JavaElement... rules) {
        use.mustNot(rules);
        return this;
    }

    public DependencyRule mustBeUsedBy(JavaElement... rules) {
        usedBy.must(rules);
        return this;
    }

    public DependencyRule mayBeUsedBy(JavaElement... rules) {
        usedBy.may(rules);
        return this;
    }

    public DependencyRule mustNotBeUsedBy(JavaElement... rules) {
        usedBy.mustNot(rules);
        return this;
    }

    public boolean isEmpty() {
        return use.isEmpty() && usedBy.isEmpty();
    }

    public <T extends UsingElement<T>> Analyzer analyzer(Model.View<T> view, DependencyRules rules) {
        return new Analyzer<>(view, rules);
    }

    public class Analyzer<T extends UsingElement<T>> {
        final RuleResult result = new RuleResult();
        private final Model.View<T> view;
        private final DependencyRules rules;
        private final List<T> elems;

        public Analyzer(Model.View<T> view, DependencyRules rules) {
            this.view = view;
            this.rules = rules;
            elems = view.matchingElements(pattern);
        }

        public RuleResult analyze() {
            analyzeNotExisting();
            analyzeMissing();
            analyzeAllowAndDeny();
            return result;
        }

        private void analyzeNotExisting() {
            if (elems.isEmpty()) {
                result.notExisting.add(pattern);
            }
        }

        private void analyzeMissing() {
            for (final T elem : elems) {
                for (final LocationMatcher mustMatcher : use.must) {
                    for (final T must : view.matchingElements(mustMatcher)) {
                        if (!elem.uses(must)) {
                            result.missing.with(pattern.specificity(), elem, must);
                        }
                    }
                }
            }
        }

        private void analyzeAllowAndDeny() {
            for (final T elem : elems) {
                for (final T dep : elem.uses()) {
                    final int allowed = calcAllowedSpecificity(elem, dep);
                    final int denied = calcDeniedSpecificity(elem, dep);
                    if (isAmbiguous(allowed, denied)) {
                        throw new AmbiguousRuleException(DependencyRule.this, elem, dep);
                    }
                    if (isAllowed(allowed, denied)) {
                        result.allowed.with(pattern.specificity(), elem, dep);
                    }
                    if (isDenied(allowed, denied)) {
                        //if deny if only because of !allowAll -> lowest specificity
                        final int spec = denied == 0 ? 0 : pattern.specificity();
                        result.denied.with(spec, elem, dep);
                    }
                }
            }
        }

        private boolean isDenied(int allowed, int denied) {
            return denied > allowed || (!allowAll && allowed == 0);
        }

        private boolean isAllowed(int allowed, int denied) {
            return allowed > denied || (allowAll && denied == 0);
        }

        private boolean isAmbiguous(int allowed, int denied) {
            return allowed != 0 && allowed == denied;
        }

        private int calcDeniedSpecificity(T thisPack, T dep) {
            return Math.max(
                    dep.mostSpecificMatch(use.mustNot),
                    rules.mostSpecificUsageMatch(thisPack, dep, MUST_NOT_BE_USED));
        }

        private int calcAllowedSpecificity(T thisPack, T dep) {
            final int useAllowed = Math.max(dep.mostSpecificMatch(use.must), dep.mostSpecificMatch(use.may));
            final int usedByAllowed = Math.max(
                    rules.mostSpecificUsageMatch(thisPack, dep, MUST_BE_USED),
                    rules.mostSpecificUsageMatch(thisPack, dep, MAY_BE_USED));
            return Math.max(useAllowed, usedByAllowed);
        }

    }

    @Override
    public String toString() {
        return "DependencyRule for " + pattern + "\n  use:      " + use + "\n  used by:  " + usedBy + "\n";
    }
}
