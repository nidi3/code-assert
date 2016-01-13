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
import guru.nidi.codeassert.model.Model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
class Usage {
    private final Set<String> must = new HashSet<>();
    private final Set<String> may = new HashSet<>();
    private final Set<String> mustNot = new HashSet<>();

    public Usage copy() {
        final Usage u = new Usage();
        u.must.addAll(must);
        u.may.addAll(may);
        u.mustNot.addAll(mustNot);
        return u;
    }

    public void must(DependencyRule... rules) {
        for (final DependencyRule rule : rules) {
            must.add(rule.name);
        }
    }

    public void may(DependencyRule... rules) {
        for (final DependencyRule rule : rules) {
            may.add(rule.name);
        }
    }

    public void mustNot(DependencyRule... rules) {
        for (final DependencyRule rule : rules) {
            mustNot.add(rule.name);
        }
    }

    public boolean isConsistent() {
        for (final String p : must) {
            final JavaPackage m = new JavaPackage(p);
            for (final String pn : mustNot) {
                final JavaPackage mn = new JavaPackage(pn);
                if (m.isMatchedBy(pn) || mn.isMatchedBy(p)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void applyUsageBy(String myName, String name, Usage by) {
        if (myName.equals(name)) {
            return;
        }
        final JavaPackage pack = new JavaPackage(myName);
        for (final String mustBy : by.must) {
            if (pack.isMatchedBy(mustBy)) {
                must.add(name);
            }
        }
        for (final String mayBy : by.may) {
            if (pack.isMatchedBy(mayBy)) {
                may.add(name);
            }
        }
        for (final String mustNotBy : by.mustNot) {
            if (pack.isMatchedBy(mustNotBy)) {
                mustNot.add(name);
            }
        }
    }

    public void analyzeDenyAll(RuleResult result, List<JavaPackage> thisPackages) {
        for (final JavaPackage thisPack : thisPackages) {
            for (final JavaPackage dep : thisPack.getUses()) {
                final boolean allowed = dep.isMatchedByAny(must) || dep.isMatchedByAny(may);
                final boolean mustNot = dep.isMatchedByAny(this.mustNot);
                if (!mustNot && allowed) {
                    result.allowed.with(thisPack, dep);
                }
                if (mustNot || !allowed) {
                    result.denied.with(thisPack, dep);
                }
            }
        }
    }

    public void analyzeAllowAll(RuleResult result, List<JavaPackage> thisPackages, Model model) {
        for (final String mustNot : this.mustNot) {
            for (final JavaPackage mustNotPack : model.matchingPackages(mustNot)) {
                for (final JavaPackage thisPack : thisPackages) {
                    if (thisPack.usesPackagesMatchedBy(mustNotPack.getName()) && !mustNotPack.isMatchedByAny(may)) {
                        result.denied.with(thisPack, mustNotPack);
                    }
                }
            }
        }
    }

    public void analyzeMissing(RuleResult result, List<JavaPackage> thisPackages, Model model) {
        for (final String must : this.must) {
            for (final JavaPackage mustPack : model.matchingPackages(must)) {
                for (final JavaPackage thisPack : thisPackages) {
                    if (!thisPack.usesPackagesMatchedBy(mustPack.getName())) {
                        result.missing.with(thisPack, mustPack);
                    }
                }
            }
        }
    }

    public boolean isEmpty() {
        return must.isEmpty() && may.isEmpty() && mustNot.isEmpty();
    }

    @Override
    public String toString() {
        return "must " + must + ", may " + may + ", must not " + mustNot;
    }
}
