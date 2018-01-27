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
package guru.nidi.codeassert.detekt;

import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import io.gitlab.arturbosch.detekt.api.Severity;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.File;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static guru.nidi.codeassert.junit.kotlin.KotlinCodeAssertMatchers.hasNoDetektIssues;
import static guru.nidi.codeassert.pmd.RegexMatcher.matchesFormat;
import static io.gitlab.arturbosch.detekt.api.Severity.Style;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DetektAnalyzerTest {
    @Test
    void analyze() {
        final DetektResult result = new DetektAnalyzer(AnalyzerConfig.maven(KOTLIN).mainAndTest(), new DetektCollector()
                .just(In.classes("Linker").ignore("MaxLineLength")))
                .analyze();
        assertMatcher(""
                        + line(Style, "style", "NewLineAtEndOfFile", "Linker", 59, "Checks whether files end with a line separator.")
                        + line(Style, "style", "WildcardImport", "Linker", 19, "Wildcard imports should be replaced with imports using fully qualified class names. Wildcard imports can lead to naming conflicts. A library update can introduce naming clashes with your classes which results in compilation errors."),
                result, hasNoDetektIssues());
    }

    private String line(Severity severity, String type, String name, String file, int line, String desc) {
        return String.format("%n%-15s %-15s %-30s %s:%d    %s",
                severity, type, name, new File("src/test/kotlin/guru/nidi/codeassert/" + file + ".kt").getAbsolutePath(), line, desc);
    }

    private <T extends AnalyzerResult<?>> void assertMatcher(String message, T result, Matcher<T> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertThat(sd.toString(), matchesFormat(message));
    }

}
