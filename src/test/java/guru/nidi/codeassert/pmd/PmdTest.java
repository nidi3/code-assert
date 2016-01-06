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

import java.io.File;

import static guru.nidi.codeassert.pmd.Rulesets.Comments.Requirement.Ignored;
import static guru.nidi.codeassert.pmd.Rulesets.Comments.Requirement.Required;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class PmdTest {
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String MAIN = "main";
    private static final String TEST = "test";

    @Test
    public void priority() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.mavenMainAndTestClasses(),
                ViolationCollector.simple(RulePriority.MEDIUM_HIGH))
                .withRuleSets(basic(), braces(), design(), optimizations(), codesize(), empty(), coupling());
        assertMatcher("" +
                        pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", 21, "A class which only has private constructors should be final") +
                        pmd(HIGH, "EmptyMethodInAbstractClassShouldBeAbstract", TEST, "model/ExampleAbstractClass", 37, "An empty method in an abstract class should be abstract instead") +
                        pmd(HIGH, "EmptyMethodInAbstractClassShouldBeAbstract", TEST, "model/ExampleAbstractClass", 41, "An empty method in an abstract class should be abstract instead"),
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
                        .ignore("AddEmptyString", "UseObjectForClearerAPI").in(PmdTest.class, FindBugsTest.class)
                        .ignoreAll().in("ExampleConcreteClass", "ExampleAbstractClass", "GenericParameters"))
                .withRuleSets(android(), basic(), braces(), cloning(), controversial(), coupling(), design(),
                        finalizers(), imports(), j2ee(), javabeans(), junit(), optimizations(),
                        exceptions(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        comments().requirement(Ignored).enums(Required).maxLines(15).maxLineLen(100),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 20).methodLen(2));
        assertMatcher("" +
                        pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", 21, "A class which only has private constructors should be final") +
                        pmd(MEDIUM, "ArrayIsStoredDirectly", MAIN, "model/AttributeInfo", 28, "The user-supplied array 'value' is stored directly.") +
                        pmd(MEDIUM, "ArrayIsStoredDirectly", MAIN, "model/ConstantPool", 29, "The user-supplied array 'pool' is stored directly.") +
                        pmd(MEDIUM, "AvoidDollarSigns", TEST, "EatYourOwnDogfoodTest", 61, "Avoid using dollar signs in variable/method/class/interface names") +
                        pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/Rulesets", 118, "The String literal \"minimum\" appears 5 times in this file; the first occurrence is on line 118") +
                        pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/Rulesets", 160, "The String literal \"CommentRequired\" appears 6 times in this file; the first occurrence is on line 160") +
                        pmd(MEDIUM, "AvoidFinalLocalVariable", MAIN, "model/JavaClassImportBuilder", 198, "Avoid using final local variables, turn them into fields") +
                        pmd(MEDIUM, "AvoidInstantiatingObjectsInLoops", MAIN, "findbugs/FindBugsMatchers", 72, "Avoid instantiating new objects inside loops") +
                        pmd(MEDIUM, "AvoidInstantiatingObjectsInLoops", MAIN, "pmd/CpdAnalyzer", 56, "Avoid instantiating new objects inside loops") +
                        pmd(MEDIUM, "AvoidLiteralsInIfCondition", MAIN, "dependency/DependencyRules", 205, "Avoid using Literals in Conditional Statements") +
                        pmd(MEDIUM, "CommentRequired", TEST, "model/p2/ExampleEnum", 18, "enumCommentRequirement Required") +
                        pmd(MEDIUM, "CommentRequired", TEST, "model/p3/ExampleSecondEnum", 18, "enumCommentRequirement Required") +
                        pmd(MEDIUM, "CommentSize", MAIN, "dependency/DependencyRuler", 18, "Comment is too large: Too many lines") +
                        pmd(MEDIUM, "CommentSize", MAIN, "dependency/DependencyRules", 54, "Comment is too large: Too many lines") +
                        pmd(MEDIUM, "ExcessiveMethodLength", TEST, "pmd/PmdTest", 61, "Avoid really long methods.") +
                        pmd(MEDIUM, "JUnitTestContainsTooManyAsserts", TEST, "model/PackageCollectorTest", 40, "JUnit tests should not contain more than 1 assert(s).") +
                        pmd(MEDIUM, "JUnitTestContainsTooManyAsserts", TEST, "model/PackageCollectorTest", 50, "JUnit tests should not contain more than 1 assert(s).") +
                        pmd(MEDIUM, "MissingStaticMethodInNonInstantiatableClass", TEST, "Bugs2", 21, "Class cannot be instantiated and does not provide any static methods or fields") +
                        pmd(MEDIUM, "SimplifyStartsWith", MAIN, "util/LocationMatcher", 89, "This call to String.startsWith can be rewritten using String.charAt(0)") +
                        pmd(MEDIUM, "SwitchStmtsShouldHaveDefault", MAIN, "model/SignatureParser", 52, "Switch statements should have a default label") +
                        pmd(MEDIUM, "TooManyMethods", MAIN, "pmd/Rulesets", 21, "This class has too many methods, consider refactoring it.") +
                        pmd(MEDIUM, "UnusedLocalVariable", TEST, "Bugs", 36, "Avoid unused local variables such as 'a'.") +
                        pmd(MEDIUM, "UseConcurrentHashMap", MAIN, "dependency/DependencyMap", 27, "If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation") +
                        pmd(MEDIUM, "UseConcurrentHashMap", MAIN, "dependency/DependencyRules", 171, "If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation") +
                        pmd(MEDIUM, "UseConcurrentHashMap", MAIN, "model/ModelAnalyzer", 42, "If you run in Java5 or newer and have concurrent access, you should use the ConcurrentHashMap implementation") +
                        pmd(MEDIUM, "UseObjectForClearerAPI", MAIN, "util/LocationMatcher", 58, "Rather than using a lot of String arguments, consider using a container object for those values."),
                analyzer, PmdMatchers.hasNoPmdViolations());
    }

    @Test
    public void cpd() {
        final CpdAnalyzer analyzer = new CpdAnalyzer(AnalyzerConfig.mavenMainClasses(), 20, new MatchCollector()
                .ignore(DependencyMap.class, RuleResult.class)
                .ignore(JavaClass.class, JavaPackage.class)
                .ignore("SignatureParser")
                .ignore(DependencyMap.class));
        assertMatcher("" +
                        cpd(23, "findbugs/BugCollector", 39, 46) +
                        cpd("pmd/ViolationCollector", 38, 45) +
                        cpd(21, "dependency/DependencyRule", 107, 108) +
                        cpd("dependency/DependencyRule", 119, 120) +
                        cpd(20, "AnalyzerConfig", 56, 57) +
                        cpd("AnalyzerConfig", 64, 64),
                analyzer, PmdMatchers.hasNoDuplications());
    }

    private <T extends Analyzer<?>> void assertMatcher(String message, T analyzer, Matcher<T> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }

    private String pmd(String priority, String name, String scope, String file, int from, String desc) {
        return String.format("%n%-11s %-45s %s:%d    %s",
                priority, name, new File("src/" + scope + "/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath(), from, desc);
    }

    private String cpd(String relative, int from, int to) {
        return cpd(0, relative, from, to);
    }

    private String cpd(int len, String file, int from, int to) {
        return "\n" + (len == 0 ? "     " : String.format("%-4d ", len)) +
                new File("src/main/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath() +
                ":" + from + "-" + to;
    }
}
