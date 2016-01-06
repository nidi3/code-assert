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
import guru.nidi.codeassert.Bugs;
import guru.nidi.codeassert.dependency.DependencyMap;
import guru.nidi.codeassert.dependency.DependencyRulesTest;
import guru.nidi.codeassert.dependency.RuleResult;
import guru.nidi.codeassert.findbugs.FindBugsTest;
import guru.nidi.codeassert.model.*;
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
                        .ignore("ShortMethodName").in(ExampleInterface.class)
                        .ignore("UnusedLocalVariable").in(Bugs.class)
                        .ignore("TooManyStaticImports").in("*Test")
                        .ignore("JUnitTestsShouldIncludeAssert").in(ClassFileParserTest.class, FileManagerTest.class, JarFileParserTest.class)
                        .ignore("JUnitTestContainsTooManyAsserts").in(DependencyRulesTest.class)
                        .ignoreAll().in("ExampleConcreteClass", "ExampleAbstractClass", "GenericParameters"))
                .withRuleSets(android(), basic(), braces(), cloning(), controversial(), coupling(), design(),
                        finalizers(), imports(), j2ee(), javabeans(), junit(), optimizations(),
                        exceptions(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        comments().requirement(Ignored).enums(Required).maxLines(15).maxLineLen(100),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 20).methodLen(2));
        assertMatcher("\n" +
                        "High        ClassWithOnlyPrivateConstructorsShouldBeFinal /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/Bugs2.java:21    A class which only has private constructors should be final\n" +
                        "Medium      ArrayIsStoredDirectly                         /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/model/AttributeInfo.java:28    The user-supplied array 'value' is stored directly.\n" +
                        "Medium      ArrayIsStoredDirectly                         /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/model/ConstantPool.java:29    The user-supplied array 'pool' is stored directly.\n" +
                        "Medium      AvoidDuplicateLiterals                        /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/pmd/Rulesets.java:118    The String literal \"minimum\" appears 5 times in this file; the first occurrence is on line 118\n" +
                        "Medium      AvoidDuplicateLiterals                        /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/pmd/Rulesets.java:160    The String literal \"CommentRequired\" appears 6 times in this file; the first occurrence is on line 160\n" +
                        "Medium      AvoidFinalLocalVariable                       /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/model/JavaClassImportBuilder.java:198    Avoid using final local variables, turn them into fields\n" +
                        "Medium      AvoidInstantiatingObjectsInLoops              /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/findbugs/FindBugsMatchers.java:72    Avoid instantiating new objects inside loops\n" +
                        "Medium      AvoidInstantiatingObjectsInLoops              /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/pmd/CpdAnalyzer.java:56    Avoid instantiating new objects inside loops\n" +
                        "Medium      AvoidLiteralsInIfCondition                    /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/dependency/DependencyRules.java:205    Avoid using Literals in Conditional Statements\n" +
                        "Medium      CommentRequired                               /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/model/p2/ExampleEnum.java:18    enumCommentRequirement Required\n" +
                        "Medium      CommentRequired                               /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/model/p3/ExampleSecondEnum.java:18    enumCommentRequirement Required\n" +
                        "Medium      CommentSize                                   /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/dependency/DependencyRules.java:54    Comment is too large: Too many lines\n" +
                        "Medium      JUnitTestContainsTooManyAsserts               /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/model/PackageCollectorTest.java:40    JUnit tests should not contain more than 1 assert(s).\n" +
                        "Medium      JUnitTestContainsTooManyAsserts               /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/model/PackageCollectorTest.java:50    JUnit tests should not contain more than 1 assert(s).\n" +
                        "Medium      MissingStaticMethodInNonInstantiatableClass   /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/Bugs2.java:21    Class cannot be instantiated and does not provide any static methods or fields\n" +
                        "Medium      SimplifyStartsWith                            /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/util/LocationMatcher.java:89    This call to String.startsWith can be rewritten using String.charAt(0)\n" +
                        "Medium      SwitchStmtsShouldHaveDefault                  /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/model/SignatureParser.java:52    Switch statements should have a default label\n" +
                        "Medium      TooManyMethods                                /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/pmd/Rulesets.java:21    This class has too many methods, consider refactoring it.\n" +
                        "Medium      UnusedLocalVariable                           /Users/nidi/idea/code-assert/src/test/java/guru/nidi/codeassert/Bugs.java:36    Avoid unused local variables such as 'a'.\n" +
                        "Medium      UseConcurrentHashMap                          /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/dependency/DependencyMap.java:27    If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation\n" +
                        "Medium      UseConcurrentHashMap                          /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/dependency/DependencyRules.java:171    If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation\n" +
                        "Medium      UseConcurrentHashMap                          /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/model/ModelAnalyzer.java:42    If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation\n" +
                        "Medium      UseObjectForClearerAPI                        /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/util/LocationMatcher.java:58    Rather than using a lot of String arguments, consider using a container object for those values.",
                analyzer, PmdMatchers.hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        final CpdAnalyzer analyzer = new CpdAnalyzer(AnalyzerConfig.mavenMainClasses(), 20, new MatchCollector()
                .ignore(DependencyMap.class, RuleResult.class)
                .ignore(JavaClass.class, JavaPackage.class)
                .ignore("SignatureParser")
                .ignore(DependencyMap.class));
        assertMatcher("\n" +
                        "23   /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/findbugs/BugCollector.java:39-46\n" +
                        "     /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/pmd/ViolationCollector.java:38-45\n" +
                        "21   /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/dependency/DependencyRule.java:107-108\n" +
                        "     /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/dependency/DependencyRule.java:119-120\n" +
                        "20   /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/AnalyzerConfig.java:56-57\n" +
                        "     /Users/nidi/idea/code-assert/src/main/java/guru/nidi/codeassert/AnalyzerConfig.java:64-64",
                analyzer, PmdMatchers.hasNoDuplications());
    }

    private <T extends Analyzer<?>> void assertMatcher(String message, T analyzer, Matcher<T> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }
}
