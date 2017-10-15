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
import guru.nidi.codeassert.model.Scope;
import guru.nidi.codeassert.model.UsingElement;

import java.util.List;

import static guru.nidi.codeassert.dependency.RuleAccessor.*;

public class DependencyRule extends CodeElement {
    final Usage use = new Usage();
    final Usage usedBy = new Usage();
    boolean optional;

    DependencyRule(String pattern, boolean allowAll) {
        super(pattern, allowAll);
    }

    public static DependencyRule allowAll(String name) {
        return new DependencyRule(name, true);
    }

    public static DependencyRule denyAll(String name) {
        return new DependencyRule(name, false);
    }

    public DependencyRule mustUse(CodeElement... rules) {
        use.must(rules);
        return this;
    }

    public DependencyRule mayUse(CodeElement... rules) {
        use.may(rules);
        return this;
    }

    public DependencyRule mustNotUse(CodeElement... rules) {
        use.mustNot(rules);
        return this;
    }

    public DependencyRule mustBeUsedBy(CodeElement... rules) {
        usedBy.must(rules);
        return this;
    }

    public DependencyRule mayBeUsedBy(CodeElement... rules) {
        usedBy.may(rules);
        return this;
    }

    public DependencyRule mustNotBeUsedBy(CodeElement... rules) {
        usedBy.mustNot(rules);
        return this;
    }

    public DependencyRule optional() {
        optional = true;
        return this;
    }

    public boolean isEmpty() {
        return use.isEmpty() && usedBy.isEmpty();
    }

    public <T extends UsingElement<T>> Analyzer analyzer(Scope<T> scope, DependencyRules rules) {
        return new Analyzer<>(scope, rules);
    }

    public class Analyzer<T extends UsingElement<T>> {
        final Dependencies result = new Dependencies();
        private final Scope<T> scope;
        private final DependencyRules rules;
        private final List<T> elems;

        public Analyzer(Scope<T> scope, DependencyRules rules) {
            this.scope = scope;
            this.rules = rules;
            elems = scope.matchingElements(pattern);
        }

        public Dependencies analyze() {
            analyzeNotExisting();
            analyzeMissing();
            analyzeAllowAndDeny();
            return result;
        }

        private void analyzeNotExisting() {
            if (!optional && elems.isEmpty()) {
                result.notExisting.add(pattern);
            }
        }

        private void analyzeMissing() {
            for (final T elem : elems) {
                for (final LocationMatcher mustMatcher : use.must) {
                    for (final T must : scope.matchingElements(mustMatcher)) {
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
                    if (isDenied(allowed, denied) && !isAllowed(elem, dep)) {
                        //if deny if only because of !allowAll -> lowest specificity
                        final int spec = denied == 0 ? 0 : pattern.specificity();
                        result.denied.with(spec, elem, dep);
                    }
                }
            }
        }

        private boolean isAllowed(T elem, T dep) {
            return rules.allowIntraPackageDeps && elem.getPackageName().equals(dep.getPackageName());
        }

        private boolean isAllowed(int allowed, int denied) {
            return allowed > denied || (allowAll && denied == 0);
        }

        private boolean isDenied(int allowed, int denied) {
            return denied > allowed || (!allowAll && allowed == 0);
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
