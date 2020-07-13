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
package guru.nidi.codeassert.checkstyle;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.EatYourOwnDogfoodTest;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.snippets.DependencyTest;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;

import static com.puppycrawl.tools.checkstyle.api.SeverityLevel.ERROR;
import static com.puppycrawl.tools.checkstyle.api.SeverityLevel.WARNING;
import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static guru.nidi.codeassert.config.CollectorConfig.just;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoCheckstyleIssues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CheckstyleTest {
    private static final String MAIN = "main";
    private static final String TEST = "test";

    private final AnalyzerConfig config = AnalyzerConfig.maven().mainAndTest();

    @BeforeAll
    static void init() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    void google() {
        final CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer(config, StyleChecks.google()
                .maxLineLen(120).indentBasic(4).indentCase(4)
                .paramName("^[a-z][a-zA-Z0-9]*$")
                .catchParamName("^[a-z][a-zA-Z0-9]*$")
                .localVarName("^[a-z][a-zA-Z0-9]*$")
                .emptyLineSeparatorTokens(IMPORT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF,
                        STATIC_INIT, INSTANCE_INIT, METHOD_DEF, CTOR_DEF, VARIABLE_DEF),
                new StyleEventCollector().severity(WARNING).config(
                        just(In.everywhere().ignore("import.avoidStar", "javadoc.missing",
                                "multiple.variable.declarations.comma", "custom.import.order.nonGroup.expected")),
                        just(In.classes("Coverage", "Constant", "DependencyRulesTest", "DependencyTest", "EatYourOwnDogfoodTest")
                                .ignore("empty.line.separator")),
                        just(In.classes("*Test").ignore("maxLineLen"))
                ));

        assertMatcher(""
                        + line(WARNING, "abbreviation.as.word", MAIN, "pmd/PmdRulesets", 165, "Abbreviation in name 'serialVersionUID' must contain no more than '1' capital letters.")
                        + line(WARNING, "abbreviation.as.word", MAIN, "pmd/PmdRulesets", 210, "Abbreviation in name 'serialVersionUID' must contain no more than '1' capital letters."),
                analyzer.analyze(), hasNoCheckstyleIssues());
    }

    @Test
    void sun() {
        final CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer(config, StyleChecks.sun()
                .maxLineLen(120).allowDefaultAccessMembers(true),
                new StyleEventCollector().severity(WARNING).config(
                        just(In.everywhere().ignore("final.parameter", "javadoc.packageInfo", "javadoc.missing",
                                "design.forExtension", "hidden.field", "import.avoidStar", "inline.conditional.avoid",
                                "magic.number")),
                        just(In.classes("*Test").ignore("maxLineLen")),
                        just(In.classes(DependencyTest.class, EatYourOwnDogfoodTest.class).ignore("name.invalidPattern"))
                ));

        assertMatcher(""
                        + line(ERROR, "final.class", TEST, "Bugs", 27, "Class InnerBugs should be declared as final.")
                        + line(ERROR, "final.class", TEST, "Bugs2", 18, "Class Bugs2 should be declared as final."),
                analyzer.analyze(), hasNoCheckstyleIssues());
    }

    private <T extends AnalyzerResult<?>> void assertMatcher(String message, T result, Matcher<T> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertEquals(message, sd.toString());
    }

    private String line(SeverityLevel severity, String key, String scope, String file, int line, String msg) {
        return String.format("%n%-8s %-40s %s:%d    %s", severity, key, new File("src/" + scope + "/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath(), line, msg);
    }
}
