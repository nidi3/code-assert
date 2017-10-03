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

import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.CoverageCollector;
import guru.nidi.codeassert.jacoco.JacocoAnalyzer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasEnoughCoverage;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled
//## codeCoverage
public class CodeCoverage {
    @Test
    public void coverage() {
        // Coverage of branches must be at least 70%, lines 80% and methods 90%
        // This is checked globally and for all packages except for entities.
        JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(BRANCH, LINE, METHOD)
                .just(For.global().setMinima(70, 80, 90))
                .just(For.allPackages().setMinima(70, 80, 90))
                .just(For.thePackage("org.proj.entity.*").setNoMinima()));
        assertThat(analyzer.analyze(), hasEnoughCoverage());
    }
}
//##
