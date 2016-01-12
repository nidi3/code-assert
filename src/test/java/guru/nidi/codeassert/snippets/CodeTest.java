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
package guru.nidi.codeassert.snippets;

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.junit.CodeAssertTest;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.model.ModelResult;
import guru.nidi.codeassert.pmd.PmdAnalyzer;
import guru.nidi.codeassert.pmd.PmdResult;
import guru.nidi.codeassert.pmd.ViolationCollector;
import org.junit.Ignore;
import org.junit.Test;

import static guru.nidi.codeassert.dependency.DependencyMatchers.matchesExactly;
import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.pmd.Rulesets.basic;
import static guru.nidi.codeassert.pmd.Rulesets.braces;
import static org.junit.Assert.assertThat;

@Ignore
//## codeTest
public class CodeTest extends CodeAssertTest {

    private static final AnalyzerConfig config = AnalyzerConfig.maven().main();

    @Test
    public void dependency() {
        class MyProject implements DependencyRuler {
            DependencyRule packages;

            @Override
            public void defineRules() {
                //TODO
            }
        }
        final DependencyRules rules = denyAll().withExternals("java*").withRules(new MyProject());
        assertThat(modelResult(), matchesExactly(rules));
    }

    @Override
    protected ModelResult analyzeModel() {
        return new ModelAnalyzer(config).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        final BugCollector bugCollector = new BugCollector().just(
                In.loc("*Exception").ignore("SE_BAD_FIELD"));
        return new FindBugsAnalyzer(config, bugCollector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final ViolationCollector collector = new ViolationCollector().just(
                In.everywhere().ignore("MethodArgumentCouldBeFinal"));
        return new PmdAnalyzer(config, collector).withRuleSets(basic(), braces()).analyze();
    }
}
//##