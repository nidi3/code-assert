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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoCycles;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.matchesRulesExactly;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled
//## dependency
public class DependencyTest {

    // Analyze all sources in src/main/java
    private final AnalyzerConfig config = AnalyzerConfig.maven().main();

    @Test
    public void noCycles() {
        assertThat(new DependencyAnalyzer(config).analyze(), hasNoCycles());
    }

    @Test
    public void dependency() {
        // Defines the dependency rules for package org.proj
        class OrgProj extends DependencyRuler {
            // Rules for org.proj.dep, org.proj.model, org.proj.util
            DependencyRule dep, model, util;

            @Override
            public void defineRules() {
                base().mayUse(util, dep.allSubOf()); //org.proj may use org.proj.util and all subpackages of org.proj.dep
                dep.andAllSub().mustUse(model); //org.proj.dep and all subpackages thereof must use org.proj.model
                model.mayUse(util).mustNotUse(base()); //org.proj.model may use org.proj.util but not org.proj
            }
        }

        // All dependencies are forbidden, except the ones defined in OrgProj
        // java, org, net packages may be used freely
        DependencyRules rules = DependencyRules.denyAll()
                .withRelativeRules(new OrgProj())
                .withExternals("java.*", "org.*", "net.*");

        DependencyResult result = new DependencyAnalyzer(config).rules(rules).analyze();
        assertThat(result, matchesRulesExactly());
    }
}
//##
