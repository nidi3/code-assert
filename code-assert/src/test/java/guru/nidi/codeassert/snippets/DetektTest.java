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
import guru.nidi.codeassert.detekt.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static guru.nidi.codeassert.junit.kotlin.KotlinCodeAssertMatchers.hasNoDetektIssues;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled
//## detekt
public class DetektTest {
    @Test
    public void analyze() {
        // Analyze all sources in src/main/kotlin
        AnalyzerConfig config = AnalyzerConfig.maven(KOTLIN).main();

        DetektCollector collector = new DetektCollector()
                .just(In.classes("Linker").ignore("MaxLineLength"));

        DetektResult result = new DetektAnalyzer(config, collector).analyze();

        assertThat(result, hasNoDetektIssues());
    }
}
//##
