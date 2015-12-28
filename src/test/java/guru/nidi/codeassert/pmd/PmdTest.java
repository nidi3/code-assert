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
package guru.nidi.codeassert.pmd;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerConfig;
import guru.nidi.codeassert.dependency.DependencyRulesTest;
import net.sourceforge.pmd.RulePriority;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class PmdTest {
    @Test
    public void priority() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.mavenMainAndTestClasses(),
                ViolationCollector.simple(RulePriority.MEDIUM_HIGH))
                .withRuleSets(basic(), braces(), design(), optimizations(), codesize(), empty())
                .withRuleSets("rulesets/java/coupling.xml");
        assertMatcher("\n" +
                        "High        ClassWithOnlyPrivateConstructorsShouldBeFinal guru.nidi.codeassert.Bugs2:21    A class which only has private constructors should be final\n" +
                        "High        EmptyMethodInAbstractClassShouldBeAbstract    guru.nidi.codeassert.model.ExampleAbstractClass:37    An empty method in an abstract class should be abstract instead\n" +
                        "High        EmptyMethodInAbstractClassShouldBeAbstract    guru.nidi.codeassert.model.ExampleAbstractClass:41    An empty method in an abstract class should be abstract instead",
                analyzer, PmdMatchers.hasNoPmdViolations());
    }

    @Test
    public void ignore() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.mavenMainAndTestClasses(),
                ViolationCollector.simple(RulePriority.MEDIUM)
                        .ignore("MethodArgumentCouldBeFinal", "LawOfDemeter", "LooseCoupling", "LocalVariableCouldBeFinal", "UncommentedEmptyConstructor", "GodClass")
                        .ignore("ExcessiveMethodLength").in(DependencyRulesTest.class))
                .withRuleSets(basic(), braces(), design(), optimizations(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        empty().emptyCatchBlock.allowCommented(true))
                .withRuleSets("rulesets/java/coupling.xml");
        assertMatcher("\n" +
                        "High        ClassWithOnlyPrivateConstructorsShouldBeFinal guru.nidi.codeassert.Bugs2:21    A class which only has private constructors should be final\n" +
                        "High        EmptyMethodInAbstractClassShouldBeAbstract    guru.nidi.codeassert.model.ExampleAbstractClass:37    An empty method in an abstract class should be abstract instead\n" +
                        "High        EmptyMethodInAbstractClassShouldBeAbstract    guru.nidi.codeassert.model.ExampleAbstractClass:41    An empty method in an abstract class should be abstract instead\n" +
                        "Medium      AvoidInstantiatingObjectsInLoops              guru.nidi.codeassert.model.JavaClassBuilder:102    Avoid instantiating new objects inside loops\n" +
                        "Medium      AvoidInstantiatingObjectsInLoops              guru.nidi.codeassert.pmd.PmdAnalyzer:66    Avoid instantiating new objects inside loops\n" +
                        "Medium      ExcessiveMethodLength                         guru.nidi.codeassert.dependency.DependencyRulesTest:177    Avoid really long methods.\n" +
                        "Medium      ImmutableField                                guru.nidi.codeassert.model.p4.GenericParameters:37    Private field 'l2' could be made final; it is only initialized in the declaration or constructor.\n" +
                        "Medium      MissingStaticMethodInNonInstantiatableClass   guru.nidi.codeassert.Bugs2:21    Class cannot be instantiated and does not provide any static methods or fields\n" +
                        "Medium      PrematureDeclaration                          guru.nidi.codeassert.model.ExampleConcreteClass:55    Avoid declaring a variable if it is unreferenced before a possible exit point.\n" +
                        "Medium      UncommentedEmptyMethodBody                    guru.nidi.codeassert.model.ExampleAbstractClass:41    Document empty method body\n" +
                        "Medium      UncommentedEmptyMethodBody                    guru.nidi.codeassert.model.ExampleConcreteClass:72    Document empty method body\n" +
                        "Medium      UncommentedEmptyMethodBody                    guru.nidi.codeassert.model.p4.GenericParameters:47    Document empty method body",
                analyzer, PmdMatchers.hasNoPmdViolations());
    }

    private <T extends Analyzer<?>> void assertMatcher(String message, T analyzer, Matcher<T> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }
}
