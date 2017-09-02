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
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.model.ModelAnalyzer;
import org.junit.*;

import java.io.IOException;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoPackageCycles;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.packagesMatchExactly;
import static org.junit.Assert.assertThat;

@Ignore
//## dependency
public class DependencyTest {

    private AnalyzerConfig config;

    @Before
    public void setup() throws IOException {
        // Analyze all sources in src/main/java
        config = AnalyzerConfig.maven().main();
    }

    @Test
    public void noCycles() {
        assertThat(new ModelAnalyzer(config).analyze(), hasNoPackageCycles());
    }

    @Test
    public void dependency() {
        // Defines the dependency rules for package org.proj
        class OrgProj extends DependencyRuler {
            // Rules for org.proj.dep, org.proj.model, org.proj.util
            DependencyRule dep, model, util;

            @Override
            public void defineRules() {
                base().mayUse(util, dep.allSub()); //org.proj may use org.proj.util and all subpackages of org.proj.dep
                dep.allSub().mustUse(model); //all subpackages of org.proj.dep must use org.proj.model
                model.mayUse(util).mustNotUse(base()); //org.proj.model may use org.proj.util but not org.proj
            }
        }

        // All dependencies are forbidden, except the ones defined in OrgProj
        // java, org, net packages are ignored
        DependencyRules rules = DependencyRules.denyAll()
                .withRelativeRules(new OrgProj())
                .withExternals("java.*", "org.*", "net.*");

        assertThat(new ModelAnalyzer(config).analyze(), packagesMatchExactly(rules));
    }
}
//##
