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
package guru.nidi.codeassert.checkstyle;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import guru.nidi.codeassert.EatYourOwnDogfoodTest;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.BaseCollector;
import guru.nidi.codeassert.config.CollectorConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyMap;
import guru.nidi.codeassert.dependency.DependencyRulesTest;
import guru.nidi.codeassert.jacoco.Coverage;
import guru.nidi.codeassert.junit.CodeAssertMatchers;
import guru.nidi.codeassert.model.ExampleConcreteClass;
import guru.nidi.codeassert.pmd.Rulesets;
import guru.nidi.codeassert.snippets.DependencyTest;
import org.junit.Test;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static guru.nidi.codeassert.config.CollectorConfig.just;
import static org.junit.Assert.assertThat;

public class CheckstyleTest {
    private final AnalyzerConfig config = AnalyzerConfig.maven().mainAndTest();

    @Test
    public void google() {
        final CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer(config, StyleChecks.GOOGLE
                .maxLineLen(125).indentBasic(4).indentCase(4)
                .paramName("^[a-z][a-zA-Z0-9]*$")
                .catchParamName("^[a-z][a-zA-Z0-9]*$")
                .localVarName("^[a-z][a-zA-Z0-9]*$")
                .emptyLineSeparatorTokens(IMPORT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF,
                        STATIC_INIT, INSTANCE_INIT, METHOD_DEF, CTOR_DEF, VARIABLE_DEF),
                new StyleEventCollector().severity(SeverityLevel.WARNING).config(
                        just(In.everywhere().ignore("import.avoidStar", "javadoc.missing",
                                "multiple.variable.declarations.comma", "custom.import.order.nonGroup.expected")),
                        just(In.locs("Coverage", "Constant", "DependencyRulesTest", "DependencyTest", "EatYourOwnDogfoodTest")
                                .ignore("empty.line.separator")),
                        just(In.loc("*Test").ignore("maxLineLen")),
                        just(In.locs("DependencyRulesTest", "ExampleAbstractClass", "ExampleConcreteClass",
                                "ExampleInterface", "SignatureParser", "DependencyTest", "EatYourOwnDogfoodTest")
                                .ignore("name.invalidPattern")),
                        just(In.clazz(Rulesets.class).ignore("abbreviation.as.word")),
                        just(In.clazz(ExampleConcreteClass.class).ignore("one.top.level.class")),
                        just(In.clazz(BaseCollector.class).ignore("overload.methods.declaration")),
                        just(In.clazz(DependencyMap.class).ignore("tag.continuation.indent"))
                ));
        assertThat(analyzer.analyze(), CodeAssertMatchers.hasNoCheckstyleIssues());
    }

    @Test
    public void sun() {
        final CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer(config, StyleChecks.SUN
                .maxLineLen(125).allowDefaultAccessMembers(true),
                new StyleEventCollector().severity(SeverityLevel.WARNING).config(
                        just(In.everywhere().ignore("final.parameter", "javadoc.packageInfo", "javadoc.missing",
                                "design.forExtension", "hidden.field", "import.avoidStar", "inline.conditional.avoid",
                                "magic.number")),
                        just(In.loc("*Test").ignore("maxLineLen")),
                        just(In.loc("Bugs*").ignore("final.class")),
                        just(In.loc("SignatureParser").ignore("assignment.inner.avoid")),
                        just(In.clazz(Coverage.class).ignore("maxParam")),
                        just(In.clazz(CollectorConfig.class).ignore("variable.notPrivate")),
                        just(In.classes(DependencyRulesTest.class, DependencyTest.class, EatYourOwnDogfoodTest.class).ignore("name.invalidPattern"))
                ));
        assertThat(analyzer.analyze(), CodeAssertMatchers.hasNoCheckstyleIssues());
    }

}
