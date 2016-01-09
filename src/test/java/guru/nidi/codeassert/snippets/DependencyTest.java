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
import guru.nidi.codeassert.dependency.DependencyMatchers;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.model.ModelAnalyzer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.config.PackageCollector.allPackages;
import static guru.nidi.codeassert.dependency.DependencyMatchers.hasNoCycles;
import static org.junit.Assert.assertThat;

@Ignore
//## dependency
public class DependencyTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        // Ignore dependencies from/to java.*, org.*, net.*
        config = AnalyzerConfig.mavenMainClasses().collecting(allPackages().excluding("java.*", "org.*", "net.*"));
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config).analyze(), hasNoCycles());
    }

    @Test
    public void dependency() {
        // Defines the dependency rules for package org.project
        class OrgProject implements DependencyRuler {
            // Rules for org.project, org.project.dependency (with sub packages), org.project.model, org.project.util
            DependencyRule $self, dependency_, model, util;

            @Override
            public void defineRules() {
                $self.mayDependUpon(util, dependency_);
                dependency_.mustDependUpon(model);
                model.mayDependUpon(util).mustNotDependUpon($self);
            }
        }

        // All dependencies are forbidden, except the ones defined in OrgProject
        DependencyRules rules = DependencyRules.denyAll().withRules(new OrgProject());

        assertThat(new ModelAnalyzer(config).analyze(), DependencyMatchers.matchesExactly(rules));
    }
}
//##