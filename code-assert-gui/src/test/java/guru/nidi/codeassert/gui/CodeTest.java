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
package guru.nidi.codeassert.gui;

import guru.nidi.codeassert.checkstyle.*;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.findbugs.*;
import guru.nidi.codeassert.junit.CodeAssertJunit5Test;
import guru.nidi.codeassert.junit.PredefConfig;
import guru.nidi.codeassert.pmd.*;
import net.sourceforge.pmd.RulePriority;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.matchesRulesExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class CodeTest extends CodeAssertJunit5Test {
    @Test
    void dependency() {
        assertThat(dependencyResult(), matchesRulesExactly());
    }

    @Override
    protected DependencyResult analyzeDependencies() {
        class GuruNidiCodeassert extends DependencyRuler {
            DependencyRule gui, model;

            public void defineRules() {
                gui.mayUse(model);
            }
        }

        final DependencyRules rules = denyAll()
                .withExternals("java.*", "org.*", "com.*")
                .withRelativeRules(new GuruNidiCodeassert());
        return new DependencyAnalyzer(AnalyzerConfig.maven().main()).rules(rules).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        return null; //TODO JVM crashes, WTF!?
//        final BugCollector bugCollector = new BugCollector()
//                .apply(PredefConfig.minimalFindBugsIgnore())
//                TODO fix
//                .just(In.clazz(AppController.class).ignore("PATH_TRAVERSAL_IN"))
//                .just(In.everywhere().ignore("SE_NO_SERIALVERSIONID", "SPRING_ENDPOINT"));
//        return new FindBugsAnalyzer(AnalyzerConfig.maven().main(), bugCollector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final PmdViolationCollector collector = new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
                .apply(PredefConfig.minimalPmdIgnore());
        return new PmdAnalyzer(AnalyzerConfig.maven().main(), collector)
                .withRulesets(PredefConfig.defaultPmdRulesets())
                .analyze();
    }

    @Override
    protected CpdResult analyzeCpd() {
        final CpdMatchCollector collector = new CpdMatchCollector()
                .apply(PredefConfig.cpdIgnoreEqualsHashCodeToString());
        return new CpdAnalyzer(AnalyzerConfig.maven().main(), 35, collector).analyze();
    }

    @Override
    protected CheckstyleResult analyzeCheckstyle() {
        final StyleEventCollector collector = new StyleEventCollector()
                .apply(PredefConfig.minimalCheckstyleIgnore());
        return new CheckstyleAnalyzer(AnalyzerConfig.maven().main(), PredefConfig.adjustedGoogleStyleChecks(), collector).analyze();
    }
}
