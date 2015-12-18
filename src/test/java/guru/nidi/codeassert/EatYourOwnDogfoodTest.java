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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.PackageCollector.all;
import static guru.nidi.codeassert.dependency.DependencyMatchers.hasNoCycles;
import static guru.nidi.codeassert.dependency.DependencyMatchers.matchesExactly;
import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.findbugs.FindBugsMatchers.hasNoIssues;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EatYourOwnDogfoodTest {
    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        config = AnalyzerConfig.mavenMainClasses().collecting(all().excluding("java.*", "org.*", "edu.*"));
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config), hasNoCycles());
    }

    @Test
    public void dependency() {
        class GuruNidiCodeassert implements DependencyRuler {
            DependencyRule self, dependency, findbugs, model;

            @Override
            public void defineRules() {
                dependency.mayDependUpon(model, self);
                findbugs.mayDependUpon(self);
                model.mayDependUpon(self);
            }
        }
        assertThat(new ModelAnalyzer(config), matchesExactly(denyAll().withRules(new GuruNidiCodeassert())));
    }

    @Test
    public void findBugs() {
        final BugCollector bugCollector = BugCollector.simple(null, null)
                .andIgnore("ClassFileParser$Constant", "SIC_INNER_SHOULD_BE_STATIC")
                .andIgnore("DependencyMatchers$CycleMatcher", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
                .andIgnore("DependencyMatchers$RuleMatcher", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
                .andIgnore("DependencyRules#withRules", "DP_DO_INSIDE_DO_PRIVILEGED")
                .andIgnore("ClassFileParser$FieldOrMethodInfo", "URF_UNREAD_FIELD")
                .andIgnore("ClassFileParser$Constant", "URF_UNREAD_FIELD");
        assertThat(new FindBugsAnalyzer(config, bugCollector), hasNoIssues());
    }
}
