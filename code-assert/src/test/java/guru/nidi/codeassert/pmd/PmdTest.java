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
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.FindBugsTest;
import net.sourceforge.pmd.RulePriority;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.File;

import static guru.nidi.codeassert.junit.CodeAssertCoreMatchers.hasNoUnusedActions;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoCodeDuplications;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoPmdViolations;
import static guru.nidi.codeassert.pmd.PmdRulesets.Comments.Requirement.Ignored;
import static guru.nidi.codeassert.pmd.PmdRulesets.Comments.Requirement.Required;
import static guru.nidi.codeassert.pmd.PmdRulesets.*;
import static guru.nidi.codeassert.pmd.RegexMatcher.matchesFormat;
import static java.lang.System.lineSeparator;
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
                        + pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", "A class which only has private constructors should be final"),
                analyzer.analyze(), hasNoPmdViolations());
    }

    @Test
    void pmdIgnore() {
        assertMatcher(""
                        + pmd(HIGH, "ClassWithOnlyPrivateConstructorsShouldBeFinal", TEST, "Bugs2", "A class which only has private constructors should be final")
                        + pmd(MEDIUM, "AssignmentInOperand", MAIN, "ktlint/KtlintAnalyzer", "Avoid assignments in operands")
                        + pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/PmdRulesets", "The String literal \"minimum\" appears 5 times in this file; the first occurrence is on line 115")
                        + pmd(MEDIUM, "AvoidDuplicateLiterals", MAIN, "pmd/PmdRulesets", "The String literal \"CommentRequired\" appears 6 times in this file; the first occurrence is on line 154")
                        + pmd(MEDIUM, "MissingStaticMethodInNonInstantiatableClass", TEST, "Bugs2", "Class cannot be instantiated and does not provide any static methods or fields")
                        + pmd(MEDIUM, "NoPackage", TEST, "/CodeCoverage", "All classes and interfaces must belong to a named package")
                        + pmd(MEDIUM, "TooManyStaticImports", MAIN, "detekt/DetektAnalyzer", "Too many static imports may lead to messy code")
                        + pmd(MEDIUM, "TooManyStaticImports", MAIN, "ktlint/KtlintAnalyzer", "Too many static imports may lead to messy code")
                        + pmd(MEDIUM, "UnusedLocalVariable", TEST, "Bugs2", "Avoid unused local variables such as 'a'.")
                        + pmd(MEDIUM, "UseObjectForClearerAPI", TEST, "detekt/DetektAnalyzerTest", "Rather than using a lot of String arguments, consider using a container object for those values.")
                        + pmd(MEDIUM, "UseProperClassLoader", MAIN, "pmd/PmdAnalyzer", "In J2EE, getClassLoader() might not work as expected.  Use Thread.currentThread().getContextClassLoader() instead."),
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
                        + cpd(39, "findbugs/FindBugsConfigs")
                        + cpd("pmd/PmdConfigs")
                        + cpd(35, "pmd/PmdAnalyzer")
                        + cpd("pmd/PmdAnalyzer")
                        + cpd(31, "detekt/DetektMatcher")
                        + cpd("ktlint/KtlintMatcher"),
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
                                "CommentRequired", "AccessorMethodGeneration"))
                        .because("They are snippets", In.packages("*.snippets.*").ignoreAll())
                        .just(
                                In.clazz(Bugs.class).ignore("UnusedLocalVariable"),
                                In.classes("*Test").ignore("TooManyStaticImports", "AvoidDollarSigns", "AddEmptyString", "DoNotCallGarbageCollectionExplicitly", "AvoidDuplicateLiterals", "JUnitTestContainsTooManyAsserts"),
                                In.clazz(DependencyRules.class).ignore("LongVariable"),
                                In.classes(PmdTest.class, FindBugsTest.class, CheckstyleTest.class).ignore("AddEmptyString", "UseObjectForClearerAPI"),
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
        return lineSeparator() + (len == 0 ? "     " : String.format("%-4d ", len))
                + new File("src/main/java/guru/nidi/codeassert/" + file + ".java").getAbsolutePath()
                + ":%d-%d";
    }
}
