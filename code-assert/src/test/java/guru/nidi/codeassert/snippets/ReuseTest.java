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

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.pmd.*;
import net.sourceforge.pmd.RulePriority;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoPmdViolations;
import static guru.nidi.codeassert.pmd.PmdRulesets.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled
public class ReuseTest {
    private final AnalyzerConfig config = AnalyzerConfig.maven().main();
    private final PmdRuleset[] rules = new PmdRuleset[]{basic(), braces(), design(), empty(), optimizations()};

    //## reuse
    private final CollectorTemplate<Ignore> pmdTestCollector = CollectorTemplate.forA(PmdViolationCollector.class)
            .because("It's a test", In.classes("*Test")
                    .ignore("JUnitSpelling", "AvoidDuplicateLiterals", "SignatureDeclareThrowsException"))
            .because("It's compiler generated code", In.languages(KOTLIN)
                    .ignore("BC_BAD_CAST_TO_ABSTRACT_COLLECTION"));

    @Test
    public void pmd() {
        PmdViolationCollector collector = new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
                .apply(pmdTestCollector)
                .because("It's not severe and occurs often", In.everywhere().ignore("MethodArgumentCouldBeFinal"));

        PmdAnalyzer analyzer = new PmdAnalyzer(config, collector).withRulesets(rules);
        assertThat(analyzer.analyze(), hasNoPmdViolations());
    }
    //##
}
