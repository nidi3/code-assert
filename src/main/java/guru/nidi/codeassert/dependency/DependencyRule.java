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

import guru.nidi.codeassert.model.JavaPackage;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public class DependencyRule {
    final String name;
    private final boolean allowAll;
    private final Usage use = new Usage();
    private final Usage usedBy = new Usage();

    DependencyRule(String name, boolean allowAll) {
        final int starPos = name.indexOf('*');
        if (starPos >= 0 && starPos != name.length() - 1) {
            throw new IllegalArgumentException("Wildcard * is only allowed at the end (e.g. java*)");
        }
        this.name = name;
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

    public boolean hasUseClause() {
        return !use.isEmpty();
    }

    public boolean matches(JavaPackage pack) {
        return pack.isMatchedBy(name);
    }

    public boolean isEmpty() {
        return use.isEmpty() && usedBy.isEmpty();
    }

    public RuleResult analyze(Collection<JavaPackage> packages, List<DependencyRule> rules) {
        final RuleResult result = new RuleResult();
        final List<JavaPackage> thisPackages = JavaPackage.allMatchesBy(packages, name);

        analyzeNotExisting(result, thisPackages);
        final Usage usage = applyUsageBy(rules);
        usage.analyzeMissing(result, thisPackages, packages);

        if (allowAll) {
            usage.analyzeAllowAll(result, thisPackages, packages);
        } else {
            usage.analyzeDenyAll(result, thisPackages);
        }

        return result;
    }

    private Usage applyUsageBy(List<DependencyRule> rules) {
        final Usage usage = use.copy();
        for (final DependencyRule rule : rules) {
            usage.applyUsageBy(name, rule.name, rule.usedBy);
        }
        if (!usage.isConsistent() || !this.usedBy.isConsistent()) {
            throw new InconsistentDependencyRuleException(this, usage);
        }
        return usage;
    }

    private void analyzeNotExisting(RuleResult result, List<JavaPackage> thisPackages) {
        if (thisPackages.isEmpty()) {
            result.notExisting.add(name);
        }
    }

    @Override
    public String toString() {
        return "DependencyRule for " + name + "\n  use:      " + use + "\n  used by:  " + usedBy + "\n";
    }
}
