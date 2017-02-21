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
package guru.nidi.codeassert.junit;

import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.dependency.DependencyCycleMatcher;
import guru.nidi.codeassert.dependency.DependencyRuleMatcher;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.FindBugsMatcher;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.jacoco.CoverageMatcher;
import guru.nidi.codeassert.jacoco.JacocoResult;
import guru.nidi.codeassert.model.JavaClass;
import guru.nidi.codeassert.model.JavaPackage;
import guru.nidi.codeassert.model.ModelResult;
import guru.nidi.codeassert.pmd.CpdMatcher;
import guru.nidi.codeassert.pmd.CpdResult;
import guru.nidi.codeassert.pmd.PmdMatcher;
import guru.nidi.codeassert.pmd.PmdResult;
import org.hamcrest.Matcher;

import java.util.Set;

public final class CodeAssertMatchers {
    private CodeAssertMatchers() {
    }

    public static Matcher<ModelResult> packagesMatchRules(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaPackage.class, rules, false, false);
    }

    public static Matcher<ModelResult> packagesMatchExactly(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaPackage.class, rules, true, true);
    }

    public static Matcher<ModelResult> packagesMatchIgnoringNonExisting(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaPackage.class, rules, false, true);
    }

    public static Matcher<ModelResult> packagesMatchIgnoringUndefined(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaPackage.class, rules, true, false);
    }

    public static Matcher<ModelResult> classesMatchRules(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaClass.class, rules, false, false);
    }

    public static Matcher<ModelResult> classesMatchExactly(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaClass.class, rules, true, true);
    }

    public static Matcher<ModelResult> classesMatchIgnoringNonExisting(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaClass.class, rules, false, true);
    }

    public static Matcher<ModelResult> classesMatchIgnoringUndefined(final DependencyRules rules) {
        return new DependencyRuleMatcher<>(JavaClass.class, rules, true, false);
    }

    public static Matcher<ModelResult> hasNoPackageCycles() {
        return new DependencyCycleMatcher<>(JavaPackage.class);
    }

    @SafeVarargs
    public static Matcher<ModelResult> hasNoPackgeCyclesExcept(Set<String>... cyclicGroups) {
        return new DependencyCycleMatcher<>(JavaPackage.class, cyclicGroups);
    }

    public static Matcher<ModelResult> hasNoClassCycles() {
        return new DependencyCycleMatcher<>(JavaClass.class);
    }

    @SafeVarargs
    public static Matcher<ModelResult> hasNoClassCyclesExcept(Set<String>... cyclicGroups) {
        return new DependencyCycleMatcher<>(JavaClass.class, cyclicGroups);
    }

    public static Matcher<FindBugsResult> hasNoBugs() {
        return new FindBugsMatcher();
    }

    public static Matcher<PmdResult> hasNoPmdViolations() {
        return new PmdMatcher();
    }

    public static Matcher<CpdResult> hasNoCodeDuplications() {
        return new CpdMatcher();
    }

    public static <T extends AnalyzerResult<?>> Matcher<T> hasNoUnusedActions() {
        return new UnusedActionsMatcher<>();
    }

    public static Matcher<JacocoResult> hasEnoughCoverage() {
        return new CoverageMatcher();
    }
}
