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
import guru.nidi.codeassert.junit.CodeAssertCoreJunit5Test;
import org.junit.jupiter.api.Disabled;

import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;

@Disabled
//## codeTest
//extend CodeAssertTest if you still use JUnit 4
public class CodeTest extends CodeAssertCoreJunit5Test {

    private static final AnalyzerConfig CONFIG = AnalyzerConfig.maven().main();

    @Override
    protected DependencyResult analyzeDependencies() {
        class MyProject extends DependencyRuler {
            DependencyRule packages;

            @Override
            public void defineRules() {
                //TODO
            }
        }

        final DependencyRules rules = denyAll().withExternals("java.*").withRelativeRules(new MyProject());
        return new DependencyAnalyzer(CONFIG).rules(rules).analyze();
    }
}
//##
