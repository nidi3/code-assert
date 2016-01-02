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
import guru.nidi.codeassert.findbugs.FindBugsTest;
import net.sourceforge.pmd.RulePriority;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static guru.nidi.codeassert.pmd.Rulesets.Comments.Requirement.Ignored;
import static guru.nidi.codeassert.pmd.Rulesets.Comments.Requirement.Required;
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
                .withRuleSets(basic(), braces(), design(), optimizations(), codesize(), empty(), coupling());
        assertMatcher("\n" +
                        "High        ClassWithOnlyPrivateConstructorsShouldBeFinal /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/Bugs2.java:21    A class which only has private constructors should be final\n" +
                        "High        EmptyMethodInAbstractClassShouldBeAbstract    /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/model/ExampleAbstractClass.java:37    An empty method in an abstract class should be abstract instead\n" +
                        "High        EmptyMethodInAbstractClassShouldBeAbstract    /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/model/ExampleAbstractClass.java:41    An empty method in an abstract class should be abstract instead",
                analyzer, PmdMatchers.hasNoPmdViolations());
    }

    @Test
    public void ignore() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.mavenMainAndTestClasses(),
                ViolationCollector.simple(RulePriority.MEDIUM)
                        .ignore("MethodArgumentCouldBeFinal", "LawOfDemeter", "LooseCoupling", "LocalVariableCouldBeFinal",
                                "UncommentedEmptyConstructor", "GodClass", "CommentDefaultAccessModifier", "AtLeastOneConstructor",
                                "OnlyOneReturn", "DefaultPackage", "CallSuperInConstructor", "AbstractNaming", "AvoidFieldNameMatchingMethodName",
                                "BeanMembersShouldSerialize", "JUnitAssertionsShouldIncludeMessage", "JUnitSpelling")
                        .ignore("ExcessiveMethodLength").in(DependencyRulesTest.class)
                        .ignore("AvoidInstantiatingObjectsInLoops").in("JavaClassBuilder", "PmdAnalyzer")
                        .ignore("AvoidDuplicateLiterals").in(DependencyRulesTest.class, FindBugsTest.class)
                        .ignoreAll().in("ExampleConcreteClass", "ExampleAbstractClass", "GenericParameters"))
                .withRuleSets(android(), basic(), braces(), cloning(), controversial(), coupling(), design(),
                        finalizers(), imports(), j2ee(), javabeans(), junit(), optimizations(),
                        exceptions(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        comments().requirement(Ignored).enums(Required).maxLines(15).maxLineLen(100),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 15).methodLen(3));
        assertMatcher("\n" +
                        "High        ClassWithOnlyPrivateConstructorsShouldBeFinal guru.nidi.codeassert.Bugs2:21    A class which only has private constructors should be final\n" +
                        "Medium      AvoidFinalLocalVariable                       guru.nidi.codeassert.model.JavaClassImportBuilder:254    Avoid using final local variables, turn them into fields\n" +
                        "Medium      AvoidLiteralsInIfCondition                    guru.nidi.codeassert.dependency.DependencyRules$Tarjan:205    Avoid using Literals in Conditional Statements\n" +
                        "Medium      AvoidLiteralsInIfCondition                    guru.nidi.codeassert.model.JavaClassImportBuilder:277    Avoid using Literals in Conditional Statements\n" +
                        "Medium      CommentRequired                               guru.nidi.codeassert.pmd.Rulesets$Comments:110    enumCommentRequirement Required\n" +
                        "Medium      CommentRequired                               guru.nidi.codeassert.model.p2.ExampleEnum:18    enumCommentRequirement Required\n" +
                        "Medium      CommentRequired                               guru.nidi.codeassert.model.p3.ExampleSecondEnum:18    enumCommentRequirement Required\n" +
                        "Medium      CommentSize                                   guru.nidi.codeassert.dependency.DependencyMap:93    Comment is too large: Line too long\n" +
                        "Medium      CommentSize                                   guru.nidi.codeassert.dependency.DependencyRules:54    Comment is too large: Too many lines\n" +
                        "Medium      ExcessiveMethodLength                         guru.nidi.codeassert.dependency.DependencyRulesTest:177    Avoid really long methods.\n" +
                        "Medium      MissingStaticMethodInNonInstantiatableClass   guru.nidi.codeassert.Bugs2:21    Class cannot be instantiated and does not provide any static methods or fields\n" +
                        "Medium      UseConcurrentHashMap                          guru.nidi.codeassert.dependency.DependencyMap:27    If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation\n" +
                        "Medium      UseConcurrentHashMap                          guru.nidi.codeassert.dependency.DependencyRules$Tarjan:171    If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation\n" +
                        "Medium      UseConcurrentHashMap                          guru.nidi.codeassert.model.ModelAnalyzer:41    If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation\n" +
                        "Medium      UseObjectForClearerAPI                        guru.nidi.codeassert.util.LocationMatcher:44    Rather than using a lot of String arguments, consider using a container object for those values.",
                analyzer, PmdMatchers.hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        final CpdAnalyzer analyzer = new CpdAnalyzer(AnalyzerConfig.mavenMainClasses(), 20);
        assertMatcher("",
                analyzer, PmdMatchers.hasNoDuplications());
    }

    private <T extends Analyzer<?>> void assertMatcher(String message, T analyzer, Matcher<T> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }
}
