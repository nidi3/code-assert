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

import static guru.nidi.codeassert.PackageCollector.all;
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
        config = AnalyzerConfig.mavenMainClasses().collecting(all().excluding("java.*", "org.*", "edu.*", "net.*"));
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config), hasNoCycles());
    }

    @Test
    public void dependency() {
        class GuruNidiCodeassert implements DependencyRuler {
            DependencyRule $self, dependency, findbugs, model, pmd, util;

            @Override
            public void defineRules() {
                $self.mayDependUpon(util);
                dependency.mayDependUpon(model, $self);
                findbugs.mayDependUpon($self, util);
                model.mayDependUpon($self, util);
                pmd.mayDependUpon($self, util);
            }
        }
        assertThat(new ModelAnalyzer(config), matchesExactly(denyAll().withRules(new GuruNidiCodeassert())));
    }

    @Test
    public void findBugs() {
        final BugCollector bugCollector = BugCollector.simple(null, null)
                .ignore("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR").in("DependencyMatchers$CycleMatcher")
                .ignore("DP_DO_INSIDE_DO_PRIVILEGED").in("DependencyRules#withRules", "Ruleset")
                .ignore("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE").in("*Comparator")
                .ignore("URF_UNREAD_FIELD").in("ClassFileParser", "Constant", "MemberInfo", "Rulesets$*", "Reason");
        assertThat(new FindBugsAnalyzer(config, bugCollector), findsNoBugs());
    }

    @Test
    public void pmd() {
        final ViolationCollector collector = ViolationCollector.simple(RulePriority.MEDIUM)
                .ignore("MethodArgumentCouldBeFinal").generally()
                .ignore("AvoidInstantiatingObjectsInLoops").in("JavaClassBuilder", "PmdAnalyzer", "CpdAnalyzer", "FindBugsMatchers$*")
                .ignore("SwitchStmtsShouldHaveDefault").in("SignatureParser")
                .ignore("TooManyMethods").in(Rulesets.class)
                .ignore("SimplifyStartsWith").in(LocationMatcher.class)
                .ignore("TooManyStaticImports").in("*Test")
                .ignore("SingularField").in("Reason")
                .ignore("GodClass").in("DependencyRules", "JavaClassImportBuilder");
        final PmdAnalyzer analyzer = new PmdAnalyzer(config, collector)
                .withRuleSets(basic(), braces(), codesize().excessiveMethodLength(40).tooManyMethods(30), design(), empty(), optimizations());
        assertThat(analyzer, hasNoPmdViolations());
    }

}
