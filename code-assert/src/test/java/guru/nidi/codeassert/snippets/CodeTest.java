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
package guru.nidi.codeassert.snippets;

import guru.nidi.codeassert.checkstyle.*;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.findbugs.*;
import guru.nidi.codeassert.junit.CodeAssertJunit5Test;
import guru.nidi.codeassert.pmd.*;
import org.junit.jupiter.api.Disabled;

import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.pmd.PmdRulesets.basic;
import static guru.nidi.codeassert.pmd.PmdRulesets.braces;

@Disabled
//## codeTest
//extend CodeAssertTest if you still use JUnit 4
public class CodeTest extends CodeAssertJunit5Test {

    private static final AnalyzerConfig CONFIG = AnalyzerConfig.maven().main();

    @Override
    protected DependencyResult analyzeDependencies() {
        class MyProject extends DependencyRuler {
            DependencyRule packages;

            @Override
            public void defineRules() {
                //TODO
            }
        }

        final DependencyRules rules = denyAll().withExternals("java.*").withRelativeRules(new MyProject());
        return new DependencyAnalyzer(CONFIG).rules(rules).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        final BugCollector bugCollector = new BugCollector().just(
                In.classes("*Exception").ignore("SE_BAD_FIELD"));
        return new FindBugsAnalyzer(CONFIG, bugCollector).analyze();
    }

    @Override
    protected CheckstyleResult analyzeCheckstyle() {
        final StyleEventCollector bugCollector = new StyleEventCollector().just(
                In.everywhere().ignore("javadoc.missing"));
        return new CheckstyleAnalyzer(CONFIG, StyleChecks.google(), bugCollector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final PmdViolationCollector collector = new PmdViolationCollector().just(
                In.everywhere().ignore("MethodArgumentCouldBeFinal"));
        return new PmdAnalyzer(CONFIG, collector).withRulesets(basic(), braces()).analyze();
    }
}
//##
