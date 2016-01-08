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
package guru.nidi.codeassert;

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.CollectorConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.pmd.PmdAnalyzer;
import guru.nidi.codeassert.pmd.Rulesets;
import guru.nidi.codeassert.pmd.ViolationCollector;
import guru.nidi.codeassert.util.LocationMatcher;
import net.sourceforge.pmd.RulePriority;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.config.PackageCollector.allPackages;
import static guru.nidi.codeassert.dependency.DependencyMatchers.hasNoCycles;
import static guru.nidi.codeassert.dependency.DependencyMatchers.matchesExactly;
import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.findbugs.FindBugsMatchers.findsNoBugs;
import static guru.nidi.codeassert.pmd.PmdMatchers.hasNoPmdViolations;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EatYourOwnDogfoodTest {
    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        config = AnalyzerConfig.mavenMainClasses().collecting(allPackages().excluding("java.*", "org.*", "edu.*", "net.*"));
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config), hasNoCycles());
    }

    @Test
    public void dependency() {
        class GuruNidiCodeassert implements DependencyRuler {
            DependencyRule $self, config, dependency, findbugs, model, pmd, util;

            @Override
            public void defineRules() {
                config.mayDependUpon(util);
                dependency.mayDependUpon(model, $self, config);
                findbugs.mayDependUpon($self, util, config);
                model.mayDependUpon($self, util, config);
                pmd.mayDependUpon($self, util, config);
            }
        }
        assertThat(new ModelAnalyzer(config), matchesExactly(denyAll().withRules(new GuruNidiCodeassert())));
    }

    @Test
    public void findBugs() {
        final BugCollector bugCollector = new BugCollector().just(
                In.loc("DependencyMatchers$CycleMatcher").ignore("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"),
                In.locs("DependencyRules#withRules", "Ruleset").ignore("DP_DO_INSIDE_DO_PRIVILEGED"),
                In.loc("*Comparator").ignore("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"),
                In.clazz(CollectorConfig.class).ignore("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"),
                In.locs("ClassFileParser", "Constant", "MemberInfo", "Rulesets$*", "Reason").ignore("URF_UNREAD_FIELD"));
        assertThat(new FindBugsAnalyzer(config, bugCollector), findsNoBugs());
    }

    @Test
    public void pmd() {
        final ViolationCollector collector =new ViolationCollector().minPriority(RulePriority.MEDIUM).just(
                In.everywhere().ignore("MethodArgumentCouldBeFinal"),
                In.locs("JavaClassBuilder", "PmdAnalyzer", "CpdAnalyzer", "FindBugsMatchers$*").ignore("AvoidInstantiatingObjectsInLoops"),
                In.loc("SignatureParser").ignore("SwitchStmtsShouldHaveDefault"),
                In.clazz(Rulesets.class).ignore("TooManyMethods"),
                In.clazz(LocationMatcher.class).ignore("SimplifyStartsWith"),
                In.loc("*Test").ignore("TooManyStaticImports"),
                In.loc("Reason").ignore("SingularField"),
                In.locs("DependencyRules", "JavaClassImportBuilder").ignore("GodClass"));
        final PmdAnalyzer analyzer = new PmdAnalyzer(config, collector)
                .withRuleSets(basic(), braces(), codesize().excessiveMethodLength(40).tooManyMethods(30), design(), empty(), optimizations());
        assertThat(analyzer, hasNoPmdViolations());
    }

}
