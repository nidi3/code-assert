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
import guru.nidi.codeassert.dependency.DependencyRulesTest;
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
                        just(In.classes("*Test").ignore("maxLineLen")),
                        just(In.classes("DependencyRulesTest", "ExampleAbstractClass", "ExampleConcreteClass",
                                "ExampleInterface", "SignatureParser", "DependencyTest", "EatYourOwnDogfoodTest")
                                .ignore("name.invalidPattern"))
                ));

        assertMatcher(""
                        + line(WARNING, "abbreviation.as.word", MAIN, "pmd/PmdRulesets", 165, "Abbreviation in name 'serialVersionUID' must contain no more than '1' capital letters.")
                        + line(WARNING, "abbreviation.as.word", MAIN, "pmd/PmdRulesets", 210, "Abbreviation in name 'serialVersionUID' must contain no more than '1' capital letters.")
                        + line(WARNING, "one.top.level.class", TEST, "model/ExampleConcreteClass", 79, "Top-level class ExamplePackageClass has to reside in its own source file.")
                        + line(WARNING, "overload.methods.declaration", MAIN, "config/BaseCollector", 53, "Overload methods should not be split. Previous overloaded method located at line '47'.")
                        + line(WARNING, "overload.methods.declaration", MAIN, "config/BaseCollector", 64, "Overload methods should not be split. Previous overloaded method located at line '51'.")
                        + line(WARNING, "overload.methods.declaration", MAIN, "model/SourceFileParser", 91, "Overload methods should not be split. Previous overloaded method located at line '66'.")
                        + line(WARNING, "tag.continuation.indent", MAIN, "dependency/DependencyMap", 105, "Line continuation have incorrect indentation level, expected level should be 4."),
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
                        just(In.classes("Bugs*").ignore("final.class")),
                        just(In.classes(DependencyRulesTest.class, DependencyTest.class, EatYourOwnDogfoodTest.class).ignore("name.invalidPattern"))
                ));

        assertMatcher(""
                        + line(ERROR, "assignment.inner.avoid", MAIN, "model/SignatureParser", 254, "Inner assignments should be avoided.")
                        + line(ERROR, "maxParam", MAIN, "jacoco/Coverage", 29, "More than 7 parameters (found 12).")
                        + line(ERROR, "variable.notPrivate", MAIN, "config/CollectorConfig", 27, "Variable 'actions' must be private and have accessor methods.")
                        + line(ERROR, "variable.notPrivate", MAIN, "model/Scope", 28, "Variable 'model' must be private and have accessor methods."),
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
