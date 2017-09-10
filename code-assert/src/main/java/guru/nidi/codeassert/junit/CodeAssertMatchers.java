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
package guru.nidi.codeassert.junit;

import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.checkstyle.CheckstyleMatcher;
import guru.nidi.codeassert.checkstyle.CheckstyleResult;
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.findbugs.FindBugsMatcher;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.jacoco.CoverageMatcher;
import guru.nidi.codeassert.jacoco.JacocoResult;
import guru.nidi.codeassert.pmd.*;
import org.hamcrest.Matcher;

public final class CodeAssertMatchers {
    private CodeAssertMatchers() {
    }

    public static Matcher<DependencyResult> matchesRules() {
        return new DependencyResultMatcher(false, false);
    }

    public static Matcher<DependencyResult> matchesRulesExactly() {
        return new DependencyResultMatcher(true, true);
    }

    public static Matcher<DependencyResult> matchesRulesIgnoringNonExisting() {
        return new DependencyResultMatcher(false, true);
    }

    public static Matcher<DependencyResult> matchesRulesIgnoringUndefined() {
        return new DependencyResultMatcher(true, false);
    }

    public static Matcher<DependencyResult> hasNoCycles() {
        return new DependencyCycleMatcher();
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

    public static Matcher<CheckstyleResult> hasNoCheckstyleIssues() {
        return new CheckstyleMatcher();
    }

    public static <T extends AnalyzerResult<?>> Matcher<T> hasNoUnusedActions() {
        return new UnusedActionsMatcher<>();
    }

    public static Matcher<JacocoResult> hasEnoughCoverage() {
        return new CoverageMatcher();
    }
}
