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
package guru.nidi.codeassert.ktlint;

import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.File;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static guru.nidi.codeassert.junit.kotlin.KotlinCodeAssertMatchers.hasNoKtlintIssues;
import static guru.nidi.codeassert.pmd.RegexMatcher.matchesFormat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KtlintAnalyzerTest {
    @Test
    void analyze() {
        final KtlintResult result = new KtlintAnalyzer(AnalyzerConfig.maven(KOTLIN).test(), new KtlintCollector()
                .just(In.classes("Linker").ignore("no-semi"))).analyze();
        assertMatcher(""
                        + line("no-unused-imports", "Linker", 18, "Unused import")
                        + line("no-wildcard-imports", "Linker", 19, "Wildcard import")
                        + line("no-consecutive-blank-lines", "Linker", 34, "Needless blank line(s)"),
                result, hasNoKtlintIssues());
    }

    private String line(String id, String file, int line, String desc) {
        return String.format("%n%-35s %s:%d    %s",
                id, new File("src/test/kotlin/guru/nidi/codeassert/" + file + ".kt").getAbsolutePath(), line, desc);
    }

    private <T extends AnalyzerResult<?>> void assertMatcher(String message, T result, Matcher<T> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertThat(sd.toString(), matchesFormat(message));
    }

}
