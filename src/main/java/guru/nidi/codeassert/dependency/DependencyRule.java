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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class DependencyRule {
    private final String name;
    private final boolean allowAll;
    private final List<String> mustDepend = new ArrayList<>();
    private final List<String> mayDepend = new ArrayList<>();
    private final List<String> mustNotDepend = new ArrayList<>();

    DependencyRule(String name, boolean allowAll) {
        final int starPos = name.indexOf("*");
        if (starPos >= 0 && (starPos != name.length() - 1 || !name.endsWith(".*"))) {
            throw new IllegalArgumentException("Wildcard * is only allowed at the end (e.g. java.*)");
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

    public DependencyRule mustDependUpon(DependencyRule... rules) {
        for (final DependencyRule rule : rules) {
            mustDepend.add(rule.name);
        }
        return this;
    }

    public DependencyRule mayDependUpon(DependencyRule... rules) {
        for (final DependencyRule rule : rules) {
            mayDepend.add(rule.name);
        }
        return this;
    }

    public DependencyRule mustNotDependUpon(DependencyRule... rules) {
        for (final DependencyRule rule : rules) {
            mustNotDepend.add(rule.name);
        }
        return this;
    }

    public boolean matches(JavaPackage pack) {
        return pack.isMatchedBy(name);
    }

    public RuleResult analyze(Collection<JavaPackage> packages) {
        final RuleResult result = new RuleResult();
        final List<JavaPackage> thisPackages = JavaPackage.allMatchesBy(packages, name);

        analyzeNotExisting(result, thisPackages);
        analyzeMissing(result, thisPackages, packages);

        if (allowAll) {
            analyzeAllowAll(result, thisPackages, packages);
        } else {
            analyzeDenyAll(result, thisPackages);
        }

        return result;
    }

    private void analyzeDenyAll(RuleResult result, List<JavaPackage> thisPackages) {
        for (final JavaPackage thisPack : thisPackages) {
            for (final JavaPackage dep : thisPack.getEfferents()) {
                final boolean allowed = dep.isMatchedByAny(mustDepend) || dep.isMatchedByAny(mayDepend);
                final boolean mustNot = dep.isMatchedByAny(mustNotDepend);
                if (!mustNot && allowed) {
                    result.allowed.with(thisPack, dep);
                }
                if (mustNot || !allowed) {
                    result.denied.with(thisPack, dep);
                }
            }
        }
    }

    private void analyzeAllowAll(RuleResult result, List<JavaPackage> thisPackages, Collection<JavaPackage> packages) {
        for (final String mustNot : mustNotDepend) {
            for (final JavaPackage mustNotPack : JavaPackage.allMatchesBy(packages, mustNot)) {
                for (final JavaPackage thisPack : thisPackages) {
                    if (thisPack.hasEfferentsMatchedBy(mustNotPack.getName()) && !mustNotPack.isMatchedByAny(mayDepend)) {
                        result.denied.with(thisPack, mustNotPack);
                    }
                }
            }
        }
    }

    private void analyzeMissing(RuleResult result, List<JavaPackage> thisPackages, Collection<JavaPackage> packages) {
        for (final String must : mustDepend) {
            for (final JavaPackage mustPack : JavaPackage.allMatchesBy(packages, must)) {
                for (final JavaPackage thisPack : thisPackages) {
                    if (!thisPack.hasEfferentsMatchedBy(mustPack.getName())) {
                        result.missing.with(thisPack, mustPack);
                    }
                }
            }
        }
    }

    private void analyzeNotExisting(RuleResult result, List<JavaPackage> thisPackages) {
        if (thisPackages.isEmpty()) {
            result.notExisting.add(name);
        }
    }

}
