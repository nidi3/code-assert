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
import guru.nidi.codeassert.config.LocationMatcherTest;
import guru.nidi.codeassert.config.LocationNameMatcherTest;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.dependency.DependencyRulesTest;
import guru.nidi.codeassert.findbugs.FindBugsTest;
import guru.nidi.codeassert.model.ClassFileParserTest;
import guru.nidi.codeassert.model.ExampleInterface;
import guru.nidi.codeassert.model.FileManagerTest;
import guru.nidi.codeassert.model.JarFileParserTest;
import net.sourceforge.pmd.RulePriority;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.io.File;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
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
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.maven().mainAndTest(),
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
                        pmd(MEDIUM, "AssignmentInOperand", MAIN, "jacoco/JacocoAnalyzer", "Avoid assignments in operands") +
                        pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/Rulesets", "The String literal \"minimum\" appears 5 times in this file; the first occurrence is on line 118") +
                        pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/Rulesets", "The String literal \"CommentRequired\" appears 6 times in this file; the first occurrence is on line 160") +
                        pmd(MEDIUM, "AvoidFinalLocalVariable", MAIN, "model/JavaClassImportBuilder", "Avoid using final local variables, turn them into fields") +
                        pmd(MEDIUM, "AvoidLiteralsInIfCondition", MAIN, "dependency/DependencyRules", "Avoid using Literals in Conditional Statements") +
                        pmd(MEDIUM, "AvoidLiteralsInIfCondition", MAIN, "pmd/PmdUtils", "Avoid using Literals in Conditional Statements") +
                        pmd(MEDIUM, "CommentRequired", TEST, "model/p2/ExampleEnum", "enumCommentRequirement Required") +
                        pmd(MEDIUM, "CommentRequired", TEST, "model/p3/ExampleSecondEnum", "enumCommentRequirement Required") +
                        pmd(MEDIUM, "ExcessiveParameterList", MAIN, "jacoco/Coverage", "Avoid long parameter lists.") +
                        pmd(MEDIUM, "MissingStaticMethodInNonInstantiatableClass", TEST, "Bugs2", "Class cannot be instantiated and does not provide any static methods or fields") +
                        pmd(MEDIUM, "NullAssignment", MAIN, "dependency/DependencyRules", "Assigning an Object to null is a code smell.  Consider refactoring.") +
                        pmd(MEDIUM, "SwitchStmtsShouldHaveDefault", MAIN, "model/SignatureParser", "Switch statements should have a default label") +
                        pmd(MEDIUM, "TooManyMethods", MAIN, "pmd/Rulesets", "This class has too many methods, consider refactoring it."),
                pmdResult, hasNoPmdViolations());
    }

    @Test
    public void pmdUnused() {
        assertThat(pmdResult, hasNoUnusedActions());
    }

    @Test
    public void duplications() {
        assertMatcher("" +
                        cpd(29, "findbugs/BugCollector") +
                        cpd("pmd/ViolationCollector") +
                        cpd(26, "model/SignatureParser") +
                        cpd("model/SignatureParser") +
                        cpd(25, "findbugs/BugCollector") +
                        cpd("jacoco/CoverageCollector") +
                        cpd("pmd/ViolationCollector"),
                cpdResult, hasNoCodeDuplications());
    }

    @Test
    public void cpdUnused() {
        assertThat(cpdResult, hasNoUnusedActions());
    }

    private PmdResult pmdAnalyze() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.maven().mainAndTest(),
                new ViolationCollector().minPriority(RulePriority.MEDIUM)
                        .because("it's not useful", In.everywhere().ignore(
                                "MethodArgumentCouldBeFinal", "LawOfDemeter", "LooseCoupling", "LocalVariableCouldBeFinal",
                                "UncommentedEmptyConstructor", "UncommentedEmptyMethodBody", "GodClass", "CommentDefaultAccessModifier",
                                "AtLeastOneConstructor", "OnlyOneReturn", "DefaultPackage", "CallSuperInConstructor", "AbstractNaming",
                                "AvoidFieldNameMatchingMethodName", "AvoidFieldNameMatchingTypeName", "BeanMembersShouldSerialize",
                                "JUnitAssertionsShouldIncludeMessage", "JUnitSpelling", "SimplifyStartsWith", "AvoidInstantiatingObjectsInLoops",
                                "UseStringBufferForStringAppends", "AvoidSynchronizedAtMethodLevel", "VariableNamingConventions"))
                        .because("They are snippets", In.loc("*.snippets.*").ignoreAll())
                        .just(
                                In.clazz(DependencyRulesTest.class).ignore("ExcessiveMethodLength"),
                                In.classes(DependencyRulesTest.class, FindBugsTest.class).ignore("AvoidDuplicateLiterals"),
                                In.clazz(ExampleInterface.class).ignore("ShortMethodName"),
                                In.clazz(Bugs.class).ignore("UnusedLocalVariable"),
                                In.locs("*Test").ignore("TooManyStaticImports", "AvoidDollarSigns", "AddEmptyString", "DoNotCallGarbageCollectionExplicitly"),
                                In.classes(ClassFileParserTest.class, FileManagerTest.class, JarFileParserTest.class).ignore("JUnitTestsShouldIncludeAssert"),
                                In.classes(DependencyRulesTest.class, LocationMatcherTest.class, LocationNameMatcherTest.class).ignore("JUnitTestContainsTooManyAsserts"),
                                In.clazz(DependencyRulesTest.class).ignore("VariableNamingConventions"),
                                In.classes(PmdTest.class, FindBugsTest.class).ignore("AddEmptyString", "UseObjectForClearerAPI"),
                                In.everywhere().ignore("UseConcurrentHashMap", "ArrayIsStoredDirectly", "MethodReturnsInternalArray"),
                                In.locs("ExampleConcreteClass", "ExampleAbstractClass", "GenericParameters").ignoreAll()))
                .withRuleSets(android(), basic(), braces(), cloning(), controversial(), coupling(), design(),
                        finalizers(), imports(), j2ee(), javabeans(), junit(), optimizations(),
                        exceptions(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        comments().requirement(Ignored).enums(Required).maxLines(35).maxLineLen(100),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 20).methodLen(2));
        return analyzer.analyze();
    }

    private CpdResult cpdAnalyze() {
        final CpdAnalyzer analyzer = new CpdAnalyzer(AnalyzerConfig.maven().main(), 25, new MatchCollector()
                .because("equals and hashCode", In.everywhere().ignore(
                        "*public boolean equals(Object o) {*",
                        "public int hashCode() {"))
                .just(
                        In.everywhere().ignore(
                                "public static <T extends AnalyzerResult<?>> Matcher<T> hasNoUnusedActions() {"),
                        In.loc("*Matcher").ignore(
                                "return item.findings().isEmpty();"),
                        In.clazz(DependencyRules.class).ignoreAll())
        );
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

    private String cpd(String relative) {
        return cpd(0, relative);
    }

    private String cpd(int len, String file) {
        return "\n" + (len == 0 ? "     " : String.format("%-4d ", len)) +
                new File("src/main/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath() +
                ":%d-%d";
    }
}
