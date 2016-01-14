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

import guru.nidi.codeassert.config.LocationMatcher;
import guru.nidi.codeassert.model.JavaPackage;
import guru.nidi.codeassert.model.Model;

import java.util.List;

/**
 */
public class DependencyRule {
    final LocationMatcher pattern;
    private final boolean allowAll;
    final Usage use = new Usage();
    final Usage usedBy = new Usage();

    DependencyRule(String pattern, boolean allowAll) {
        final int starPos = pattern.indexOf('*');
        if (starPos >= 0 && starPos != pattern.length() - 1) {
            throw new IllegalArgumentException("Wildcard * is only allowed at the end (e.g. java*)");
        }
        this.pattern = new LocationMatcher(pattern);
        this.allowAll = allowAll;
    }

    public static DependencyRule allowAll(String name) {
        return new DependencyRule(name, true);
    }

    public static DependencyRule denyAll(String name) {
        return new DependencyRule(name, false);
    }

    public DependencyRule mustUse(DependencyRule... rules) {
        use.must(rules);
        return this;
    }

    public DependencyRule mayUse(DependencyRule... rules) {
        use.may(rules);
        return this;
    }

    public DependencyRule mustNotUse(DependencyRule... rules) {
        use.mustNot(rules);
        return this;
    }

    public DependencyRule mustBeUsedBy(DependencyRule... rules) {
        usedBy.must(rules);
        return this;
    }

    public DependencyRule mayBeUsedBy(DependencyRule... rules) {
        usedBy.may(rules);
        return this;
    }

    public DependencyRule mustNotBeUsedBy(DependencyRule... rules) {
        usedBy.mustNot(rules);
        return this;
    }

    public boolean matches(JavaPackage pack) {
        return pattern.matches(pack.getName());
    }

    public boolean isEmpty() {
        return use.isEmpty() && usedBy.isEmpty();
    }

    public RuleResult analyze(Model model, DependencyRules rules) {
        final RuleResult result = new RuleResult();
        final List<JavaPackage> thisPackages = model.matchingPackages(pattern);

        analyzeNotExisting(result, thisPackages);
        analyzeMissing(result, thisPackages, model);
        analyzeAllowAndDeny(result, thisPackages, rules);

        return result;
    }

    private void analyzeAllowAndDeny(RuleResult result, List<JavaPackage> thisPackages, DependencyRules rules) {
        for (final JavaPackage thisPack : thisPackages) {
            for (final JavaPackage dep : thisPack.getUses()) {
                final int allowed = calcAllowedSpecificity(rules, thisPack, dep);
                final int denied = calcDeniedSpecificity(rules, thisPack, dep);
                if (isAmbiguous(allowed, denied)) {
                    throw new AmbiguousRuleException(this, thisPack, dep);
                }
                if (isAllowed(allowed, denied)) {
                    result.allowed.with(pattern.specificity(), thisPack, dep);
                }
                if (isDenied(allowed, denied)) {
                    //if deny if only because of !allowAll -> lowest specificity
                    final int spec = denied == 0 ? 0 : pattern.specificity();
                    result.denied.with(spec, thisPack, dep);
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

    private int calcDeniedSpecificity(DependencyRules rules, JavaPackage thisPack, JavaPackage dep) {
        return Math.max(dep.mostSpecificMatch(use.mustNot), rules.mostSpecificMustNotBeUsedMatch(thisPack, dep));
    }

    private int calcAllowedSpecificity(DependencyRules rules, JavaPackage thisPack, JavaPackage dep) {
        final int useAllowed = Math.max(dep.mostSpecificMatch(use.must), dep.mostSpecificMatch(use.may));
        final int usedByAllowed = Math.max(rules.mostSpecificMustBeUsedMatch(thisPack, dep), rules.mostSpecificMayBeUsedMatch(thisPack, dep));
        return Math.max(useAllowed, usedByAllowed);
    }

    private void analyzeMissing(RuleResult result, List<JavaPackage> thisPackages, Model model) {
        for (final JavaPackage thisPack : thisPackages) {
            for (final LocationMatcher must : use.must) {
                for (final JavaPackage mustPack : model.matchingPackages(must)) {
                    if (!thisPack.uses(mustPack)) {
                        result.missing.with(pattern.specificity(), thisPack, mustPack);
                    }
                }
            }
        }
    }

    private void analyzeNotExisting(RuleResult result, List<JavaPackage> thisPackages) {
        if (thisPackages.isEmpty()) {
            result.notExisting.add(pattern);
        }
    }

    @Override
    public String toString() {
        return "DependencyRule for " + pattern + "\n  use:      " + use + "\n  used by:  " + usedBy + "\n";
    }
}
