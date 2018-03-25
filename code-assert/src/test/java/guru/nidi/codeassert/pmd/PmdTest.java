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
package guru.nidi.codeassert.pmd;

import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.Bugs;
import guru.nidi.codeassert.checkstyle.CheckstyleTest;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.dependency.DependencyRulesTest;
import guru.nidi.codeassert.findbugs.FindBugsTest;
import guru.nidi.codeassert.model.ClassFileParserTest;
import guru.nidi.codeassert.model.ExampleInterface;
import net.sourceforge.pmd.RulePriority;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.File;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
import static guru.nidi.codeassert.pmd.PmdRulesets.Comments.Requirement.Ignored;
import static guru.nidi.codeassert.pmd.PmdRulesets.Comments.Requirement.Required;
import static guru.nidi.codeassert.pmd.PmdRulesets.*;
import static guru.nidi.codeassert.pmd.RegexMatcher.matchesFormat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PmdTest {
    private static final String MEDIUM = "Medium";
    private static final String HIGH = "High";
    private static final String MAIN = "main";
    private static final String TEST = "test";

    private final CpdResult cpdResult = cpdAnalyze();
    private final PmdResult pmdResult = pmdAnalyze();

    @Test
    void priority() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.maven().mainAndTest(),
                new PmdViolationCollector().minPriority(RulePriority.MEDIUM_HIGH))
                .withRulesets(basic(), braces(), design(), optimizations(), codesize(), empty(), coupling());
        assertMatcher(""
                        + pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", "A class which only has private constructors should be final")
                        + pmd(HIGH, "EmptyMethodInAbstractClassShouldBeAbstract", TEST, "model/ExampleAbstractClass", "An empty method in an abstract class should be abstract instead")
                        + pmd(HIGH, "EmptyMethodInAbstractClassShouldBeAbstract", TEST, "model/ExampleAbstractClass", "An empty method in an abstract class should be abstract instead"),
                analyzer.analyze(), hasNoPmdViolations());
    }

    @Test
    void pmdIgnore() {
        assertMatcher(""
                        + pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", "A class which only has private constructors should be final")
                        + pmd(MEDIUM, "AssignmentInOperand", MAIN, "jacoco/JacocoAnalyzer", "Avoid assignments in operands")
                        + pmd(MEDIUM, "AssignmentInOperand", MAIN, "ktlint/KtlintAnalyzer", "Avoid assignments in operands")
                        + pmd(MEDIUM, "AssignmentInOperand", MAIN, "model/Model", "Avoid assignments in operands")
                        + pmd(MEDIUM, "AssignmentInOperand", MAIN, "model/SourceFileParser", "Avoid assignments in operands")
                        + pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/PmdRulesets", "The String literal \"minimum\" appears 5 times in this file; the first occurrence is on line 115")
                        + pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/PmdRulesets", "The String literal \"CommentRequired\" appears 6 times in this file; the first occurrence is on line 154")
                        + pmd(MEDIUM, "AvoidFinalLocalVariable", MAIN, "model/CodeClassBuilder", "Avoid using final local variables, turn them into fields")
                        + pmd(MEDIUM, "CommentSize", MAIN, "config/LocationNameMatcher", "Comment is too large: Line too long")
                        + pmd(MEDIUM, "ConfusingTernary", MAIN, "config/Location", "Avoid if (x != y) ..; else ..;")
                        + pmd(MEDIUM, "ExcessiveParameterList", MAIN, "jacoco/Coverage", "Avoid long parameter lists.")
                        + pmd(MEDIUM, "InefficientEmptyStringCheck", MAIN, "model/SourceFileParser", "String.trim().length()==0 is an inefficient way to validate an empty String.")
                        + pmd(MEDIUM, "LongVariable", MAIN, "dependency/Tarjan", "Avoid excessively long variable names like allowIntraPackageCycles")
                        + pmd(MEDIUM, "MissingStaticMethodInNonInstantiatableClass", TEST, "Bugs2", "Class cannot be instantiated and does not provide any static methods or fields")
                        + pmd(MEDIUM, "NoPackage", TEST, "/CodeCoverage", "All classes and interfaces must belong to a named package")
                        + pmd(MEDIUM, "NullAssignment", MAIN, "dependency/DependencyRules", "Assigning an Object to null is a code smell.  Consider refactoring.")
                        + pmd(MEDIUM, "TooManyMethods", MAIN, "pmd/PmdRulesets", "This class has too many methods, consider refactoring it.")
                        + pmd(MEDIUM, "TooManyMethods", TEST, "config/LocationMatcherTest", "This class has too many methods, consider refactoring it.")
                        + pmd(MEDIUM, "UseObjectForClearerAPI", TEST, "detekt/DetektAnalyzerTest", "Rather than using a lot of String arguments, consider using a container object for those values."),
                pmdResult, hasNoPmdViolations());
    }

    @Test
    void pmdUnused() {
        assertThat(pmdResult, hasNoUnusedActions());
    }

    @Test
    void duplications() {
        assertMatcher(""
                        + cpd(42, "detekt/DetektCollector")
                        + cpd("ktlint/KtlintCollector")
                        + cpd(40, "pmd/PmdAnalyzer")
                        + cpd("pmd/PmdAnalyzer")
                        + cpd(35, "dependency/DependencyCollector")
                        + cpd("detekt/DetektCollector")
                        + cpd("ktlint/KtlintCollector")
                        + cpd(29, "detekt/DetektMatcher")
                        + cpd("ktlint/KtlintMatcher")
                        + cpd(26, "checkstyle/StyleEventCollector")
                        + cpd("detekt/DetektCollector")
                        + cpd("findbugs/BugCollector")
                        + cpd("jacoco/CoverageCollector")
                        + cpd("ktlint/KtlintCollector")
                        + cpd("pmd/PmdViolationCollector"),
                cpdResult, hasNoCodeDuplications());
    }

    @Test
    void cpdUnused() {
        assertThat(cpdResult, hasNoUnusedActions());
    }

    private PmdResult pmdAnalyze() {
        final PmdAnalyzer analyzer = new PmdAnalyzer(AnalyzerConfig.maven().mainAndTest(),
                new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
                        .because("it's not useful", In.everywhere().ignore(
                                "MethodArgumentCouldBeFinal", "LawOfDemeter", "LooseCoupling", "LocalVariableCouldBeFinal",
                                "UncommentedEmptyConstructor", "UncommentedEmptyMethodBody", "GodClass", "CommentDefaultAccessModifier",
                                "AtLeastOneConstructor", "OnlyOneReturn", "DefaultPackage", "CallSuperInConstructor", "AbstractNaming",
                                "AvoidFieldNameMatchingMethodName", "AvoidFieldNameMatchingTypeName", "BeanMembersShouldSerialize",
                                "JUnitAssertionsShouldIncludeMessage", "JUnitSpelling", "SimplifyStartsWith", "AvoidInstantiatingObjectsInLoops",
                                "UseStringBufferForStringAppends", "AvoidSynchronizedAtMethodLevel", "VariableNamingConventions",
                                "CommentRequired"))
                        .because("They are snippets", In.packages("*.snippets.*").ignoreAll())
                        .just(
                                In.clazz(DependencyRulesTest.class).ignore("ExcessiveMethodLength"),
                                In.classes(DependencyRulesTest.class, FindBugsTest.class).ignore("AvoidDuplicateLiterals"),
                                In.clazz(ExampleInterface.class).ignore("ShortMethodName"),
                                In.clazz(Bugs.class).ignore("UnusedLocalVariable"),
                                In.classes("*Test").ignore("TooManyStaticImports", "AvoidDollarSigns", "AddEmptyString", "DoNotCallGarbageCollectionExplicitly", "AvoidDuplicateLiterals", "JUnitTestContainsTooManyAsserts"),
                                In.classes(ClassFileParserTest.class).ignore("JUnitTestsShouldIncludeAssert", "JUnitTestContainsTooManyAsserts"),
                                In.classes(DependencyRulesTest.class, LocationMatcherTest.class, LocationNameMatcherTest.class).ignore("JUnitTestContainsTooManyAsserts"),
                                In.clazz(DependencyRulesTest.class).ignore("VariableNamingConventions"),
                                In.clazz(DependencyRules.class).ignore("LongVariable"),
                                In.classes(PmdTest.class, FindBugsTest.class, CheckstyleTest.class).ignore("AddEmptyString", "UseObjectForClearerAPI"),
                                In.classes(AnalyzerConfigTest.class).ignore("JUnitTestContainsTooManyAsserts"),
                                In.classes("SourceFileParser", "Location", "LocationMatcher").ignore("CyclomaticComplexity", "StdCyclomaticComplexity", "ModifiedCyclomaticComplexity"),
                                In.everywhere().ignore("UseConcurrentHashMap", "ArrayIsStoredDirectly", "MethodReturnsInternalArray", "AvoidLiteralsInIfCondition"),
                                In.classes("ExampleConcreteClass", "ExampleAbstractClass", "GenericParameters").ignoreAll()))
                .withRulesets(android(), basic(), braces(), cloning(), controversial(), coupling(), design(),
                        finalizers(), imports(), j2ee(), javabeans(), junit(), optimizations(),
                        exceptions(), strings(), sunSecure(), typeResolution(), unnecessary(), unused(),
                        codesize().excessiveMethodLength(50).tooManyMethods(30),
                        comments().requirement(Ignored).enums(Required).maxLines(35).maxLineLen(100),
                        empty().allowCommentedEmptyCatch(true),
                        naming().variableLen(1, 20).methodLen(2));
        return analyzer.analyze();
    }

    private CpdResult cpdAnalyze() {
        final CpdAnalyzer analyzer = new CpdAnalyzer(AnalyzerConfig.maven().main(), 25, new CpdMatchCollector()
                .because("equals and hashCode", In.everywhere().ignore(
                        "*public boolean equals(Object o) {*",
                        "public int hashCode() {"))
                .just(
                        In.everywhere().ignore(
                                "public static <T extends AnalyzerResult<?>> Matcher<T> hasNoUnusedActions() {"),
                        In.classes("*Matcher").ignore(
                                "return item.findings().isEmpty();"),
                        In.classes("DependencyRules", "ProjectLayout", "SourceFileParser").ignoreAll())
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
        final String filename = file.startsWith("/") ? file : ("/guru/nidi/codeassert/" + file);
        return String.format("%n%-11s %-45s %s:%%d    %s",
                priority, name, new File("src/" + scope + "/java" + filename + ".java").getAbsolutePath(), desc);
    }

    private String cpd(String relative) {
        return cpd(0, relative);
    }

    private String cpd(int len, String file) {
        return "\n" + (len == 0 ? "     " : String.format("%-4d ", len))
                + new File("src/main/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath()
                + ":%d-%d";
    }
}
