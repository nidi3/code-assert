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

import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.Bugs;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
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

import static guru.nidi.codeassert.pmd.PmdMatchers.hasNoPmdViolations;
import static guru.nidi.codeassert.pmd.PmdMatchers.hasNoUnusedActions;
import static guru.nidi.codeassert.pmd.RegexMatcher.matchesFormat;
import static guru.nidi.codeassert.pmd.Rulesets.Comments.Requirement.Ignored;
import static guru.nidi.codeassert.pmd.Rulesets.Comments.Requirement.Required;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class PmdTest {
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String MAIN = "main";
    private static final String TEST = "test";

    private final CpdResult cpdResult = cpdAnalyze();
    private final PmdResult pmdResult = pmdAnalyze();

    @Test
    public void priority() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.mavenMainAndTestClasses(),
                new ViolationCollector().minPriority(RulePriority.MEDIUM_HIGH))
                .withRuleSets(basic(), braces(), design(), optimizations(), codesize(), empty(), coupling());
        assertMatcher("" +
                        pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", "A class which only has private constructors should be final") +
                        pmd(HIGH, "EmptyMethodInAbstractClassShouldBeAbstract", TEST, "model/ExampleAbstractClass", "An empty method in an abstract class should be abstract instead") +
                        pmd(HIGH, "EmptyMethodInAbstractClassShouldBeAbstract", TEST, "model/ExampleAbstractClass", "An empty method in an abstract class should be abstract instead"),
                analyzer.analyze(), hasNoPmdViolations());
    }

    @Test
    public void pmdIgnore() {
        assertMatcher("" +
                        pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", "A class which only has private constructors should be final") +
                        pmd(MEDIUM, "ArrayIsStoredDirectly", MAIN, "model/AttributeInfo", "The user-supplied array 'value' is stored directly.") +
                        pmd(MEDIUM, "ArrayIsStoredDirectly", MAIN, "model/ConstantPool", "The user-supplied array 'pool' is stored directly.") +
                        pmd(MEDIUM, "AvoidDollarSigns", TEST, "EatYourOwnDogfoodTest", "Avoid using dollar signs in variable/method/class/interface names") +
                        pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/Rulesets", "The String literal \"minimum\" appears 5 times in this file; the first occurrence is on line 118") +
                        pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/Rulesets", "The String literal \"CommentRequired\" appears 6 times in this file; the first occurrence is on line 160") +
                        pmd(MEDIUM, "AvoidFinalLocalVariable", MAIN, "model/JavaClassImportBuilder", "Avoid using final local variables, turn them into fields") +
                        pmd(MEDIUM, "AvoidLiteralsInIfCondition", MAIN, "dependency/DependencyRules", "Avoid using Literals in Conditional Statements") +
                        pmd(MEDIUM, "AvoidLiteralsInIfCondition", MAIN, "pmd/PmdUtils", "Avoid using Literals in Conditional Statements") +
                        pmd(MEDIUM, "CommentRequired", TEST, "model/p2/ExampleEnum", "enumCommentRequirement Required") +
                        pmd(MEDIUM, "CommentRequired", TEST, "model/p3/ExampleSecondEnum", "enumCommentRequirement Required") +
                        pmd(MEDIUM, "CommentSize", MAIN, "dependency/DependencyRuler", "Comment is too large: Too many lines") +
                        pmd(MEDIUM, "CommentSize", MAIN, "dependency/DependencyRules", "Comment is too large: Too many lines") +
                        pmd(MEDIUM, "MissingStaticMethodInNonInstantiatableClass", TEST, "Bugs2", "Class cannot be instantiated and does not provide any static methods or fields") +
                        pmd(MEDIUM, "NullAssignment", MAIN, "dependency/DependencyRules", "Assigning an Object to null is a code smell.  Consider refactoring.") +
                        pmd(MEDIUM, "SwitchStmtsShouldHaveDefault", MAIN, "model/SignatureParser", "Switch statements should have a default label") +
                        pmd(MEDIUM, "TooManyMethods", MAIN, "pmd/Rulesets", "This class has too many methods, consider refactoring it.") +
                        pmd(MEDIUM, "UnusedLocalVariable", TEST, "Bugs", "Avoid unused local variables such as 'a'.") +
                        pmd(MEDIUM, "UseObjectForClearerAPI", MAIN, "util/LocationMatcher", "Rather than using a lot of String arguments, consider using a container object for those values."),
                pmdResult, hasNoPmdViolations());
    }

    @Test
    public void pmdUnused() {
        assertThat(pmdResult, hasNoUnusedActions());
    }

    @Test
    public void duplications() {
        assertMatcher("" +
                        cpd(21, "config/AnalyzerConfig", 53, 54) +
                        cpd("config/AnalyzerConfig", 60, 60) +
                        cpd(21, "dependency/Usage", 109, 110) +
                        cpd("dependency/Usage", 121, 122),
                cpdResult, PmdMatchers.hasNoDuplications());
    }

    @Test
    public void cpdUnused() {
        assertThat(cpdResult, hasNoUnusedActions());
    }

    private PmdResult pmdAnalyze() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.mavenMainAndTestClasses(),
                new ViolationCollector().minPriority(RulePriority.MEDIUM)
                        .because("it's not useful", In.everywhere().ignore(
                                "MethodArgumentCouldBeFinal", "LawOfDemeter", "LooseCoupling", "LocalVariableCouldBeFinal",
                                "UncommentedEmptyConstructor", "UncommentedEmptyMethodBody", "GodClass", "CommentDefaultAccessModifier",
                                "AtLeastOneConstructor", "OnlyOneReturn", "DefaultPackage", "CallSuperInConstructor", "AbstractNaming",
                                "AvoidFieldNameMatchingMethodName", "AvoidFieldNameMatchingTypeName", "BeanMembersShouldSerialize",
                                "JUnitAssertionsShouldIncludeMessage", "JUnitSpelling", "SimplifyStartsWith", "AvoidInstantiatingObjectsInLoops"))
                        .because("They are snippets", In.loc("*.snippets.*").ignoreAll())
                        .just(In.clazz(DependencyRulesTest.class).ignore("ExcessiveMethodLength"),
                                In.classes(DependencyRulesTest.class, FindBugsTest.class).ignore("AvoidDuplicateLiterals"),
                                In.clazz(ExampleInterface.class).ignore("ShortMethodName"),
                                In.clazz(Bugs.class).ignore("UnusedLocalVariable"),
                                In.loc("*Test").ignore("TooManyStaticImports"),
                                In.classes(ClassFileParserTest.class, FileManagerTest.class, JarFileParserTest.class).ignore("JUnitTestsShouldIncludeAssert"),
                                In.clazz(DependencyRulesTest.class).ignore("JUnitTestContainsTooManyAsserts"),
                                In.loc("DependencyRulesTest$*").ignore("VariableNamingConventions"),
                                In.classes(PmdTest.class, FindBugsTest.class).ignore("AddEmptyString", "UseObjectForClearerAPI"),
                                In.everywhere().ignore("UseConcurrentHashMap"),
                                In.locs("ExampleConcreteClass", "ExampleAbstractClass", "GenericParameters").ignoreAll()))
                .withRuleSets(android(), basic(), braces(), cloning(), controversial(), coupling(), design(),
                        finalizers(), imports(), j2ee(), javabeans(), junit(), optimizations(),
                        exceptions(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        comments().requirement(Ignored).enums(Required).maxLines(15).maxLineLen(100),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 20).methodLen(2));
        return analyzer.analyze();
    }

    private CpdResult cpdAnalyze() {
        final CpdAnalyzer analyzer = new CpdAnalyzer(AnalyzerConfig.mavenMainClasses(), 20, new MatchCollector()
                .because("blaj",
                        In.classes(DependencyMap.class, RuleResult.class).ignoreAll())
                .just(In.classes(JavaClass.class, JavaPackage.class).ignoreAll(),
                        In.loc("SignatureParser").ignoreAll(),
                        In.loc("*Collector").ignoreAll(),
                        In.loc("*Matchers").ignoreAll(),
                        In.loc("*Result").ignoreAll(),
                        In.clazz(DependencyMap.class).ignoreAll()));
        return analyzer.analyze();
    }

    private <T extends AnalyzerResult<?>> void assertMatcher(String message, T result, Matcher<T> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertThat(sd.toString(), matchesFormat(message));
    }

    private String pmd(String priority, String name, String scope, String file, String desc) {
        return String.format("%n%-11s %-45s %s:%%d    %s",
                priority, name, new File("src/" + scope + "/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath(), desc);
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
