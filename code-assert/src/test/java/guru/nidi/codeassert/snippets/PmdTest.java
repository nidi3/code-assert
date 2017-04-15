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

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyMap;
import guru.nidi.codeassert.dependency.RuleResult;
import guru.nidi.codeassert.pmd.CpdAnalyzer;
import guru.nidi.codeassert.pmd.CpdMatchCollector;
import guru.nidi.codeassert.pmd.PmdAnalyzer;
import guru.nidi.codeassert.pmd.PmdViolationCollector;
import net.sourceforge.pmd.RulePriority;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoCodeDuplications;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoPmdViolations;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

@Ignore
//## pmd
public class PmdTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        config = AnalyzerConfig.maven().main();
    }

    @Test
    public void pmd() {
        // Only treat violations with MEDIUM priority or higher
        // Ignore the given violations in the given classes / methods
        PmdViolationCollector collector = new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
                .because("It's not severe and occurs very often",
                        In.everywhere().ignore("MethodArgumentCouldBeFinal"),
                        In.locs("JavaClassBuilder#build", "FindBugsMatchers").ignore("AvoidInstantiatingObjectsInLoops"))
                .because("it'a an enum",
                        In.loc("SignatureParser").ignore("SwitchStmtsShouldHaveDefault"))
                .just(In.loc("*Test").ignore("TooManyStaticImports"));

        // Define and configure the rule sets to be used
        PmdAnalyzer analyzer = new PmdAnalyzer(config, collector).withRulesets(
                basic(), braces(), design(), empty(), optimizations(),
                codesize().excessiveMethodLength(40).tooManyMethods(30));

        assertThat(analyzer.analyze(), hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        // Ignore duplications in the given classes
        CpdMatchCollector collector = new CpdMatchCollector()
                .because("equals",
                        In.everywhere().ignore("public boolean equals(Object o) {"))
                .just(
                        In.classes(DependencyMap.class, RuleResult.class).ignoreAll(),
                        In.loc("SignatureParser").ignoreAll());

        // Only treat duplications with at least 20 tokens
        CpdAnalyzer analyzer = new CpdAnalyzer(config, 20, collector);

        assertThat(analyzer.analyze(), hasNoCodeDuplications());
    }
}
//##
