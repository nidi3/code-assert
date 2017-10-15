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
package guru.nidi.codeassert.junit;

import guru.nidi.codeassert.checkstyle.StyleChecks;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.pmd.*;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static guru.nidi.codeassert.config.Language.KOTLIN;
import static guru.nidi.codeassert.pmd.PmdRulesets.*;

public final class PredefConfig {
    private PredefConfig() {
    }

    public static CollectorTemplate<Ignore> minimalPmdIgnore() {
        return CollectorTemplate.forA(PmdViolationCollector.class)
                .because("junit", In.classes("*Test", "Test*")
                        .ignore("JUnitSpelling", "JUnitAssertionsShouldIncludeMessage", "AvoidDuplicateLiterals",
                                "SignatureDeclareThrowsException", "TooManyStaticImports"))
                .because("I don't agree", In.everywhere()
                        .ignore("MethodArgumentCouldBeFinal", "AvoidFieldNameMatchingMethodName",
                                "CommentDefaultAccessModifier", "AbstractNaming", "AvoidFieldNameMatchingTypeName",
                                "UncommentedEmptyConstructor", "UseStringBufferForStringAppends",
                                "UncommentedEmptyMethodBody", "EmptyMethodInAbstractClassShouldBeAbstract",
                                "InefficientEmptyStringCheck"))
                .because("it's equals", In.methods("equals")
                        .ignore("NPathComplexity", "ModifiedCyclomaticComplexity", "StdCyclomaticComplexity",
                                "CyclomaticComplexity", "ConfusingTernary"))
                .because("it's hashCode", In.methods("hashCode")
                        .ignore("ConfusingTernary"));
    }

    //valid for both PMD and findBugs
    public static CollectorTemplate<Ignore> dependencyTestIgnore(Class<?> dependencyTest) {
        return CollectorTemplate.of(Ignore.class)
                .just(In.clazz(dependencyTest).ignore(
                        "AvoidDollarSigns", "VariableNamingConventions", "SuspiciousConstantFieldName",
                        "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD"));
    }

    public static CollectorTemplate<Ignore> cpdIgnoreEqualsHashCodeToString() {
        return CollectorTemplate.forA(CpdMatchCollector.class)
                .because("equals, hashCode, toString sometimes look the same", In.everywhere()
                        .ignore("public boolean equals(Object", "public int hashCode()", "public String toString()"));
    }

    public static CollectorTemplate<Ignore> minimalFindBugsIgnore() {
        return CollectorTemplate.forA(BugCollector.class)
                .because("modern compilers are clever", In.everywhere().ignore(
                        "SBSC_USE_STRINGBUFFER_CONCATENATION"))
                .because("it's compiler generated code", In.languages(KOTLIN).ignore(
                        "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "BC_BAD_CAST_TO_ABSTRACT_COLLECTION"))
                .because("it's compiler generated code, but why?", In.languages(KOTLIN).ignore(
                        "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE"))
                .because("findbugs seems to be cleverer than kotlin compiler", In.languages(KOTLIN).ignore(
                        "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"))
                .because("inline methods seem to cause this", In.languages(KOTLIN).ignore(
                        "UPM_UNCALLED_PRIVATE_METHOD"));
    }

    public static PmdRuleset[] defaultPmdRulesets() {
        return new PmdRuleset[]{
                basic(), braces(),
                comments().maxLines(35).maxLineLen(120).requirement(PmdRulesets.Comments.Requirement.Ignored),
                codesize().excessiveMethodLength(40).tooManyMethods(30),
                design(), empty().allowCommentedEmptyCatch(true), exceptions(), imports(), junit(),
                naming().variableLen(1, 25).methodLen(2),
                optimizations(), strings(),
                sunSecure(), typeResolution(), unnecessary(), unused()};
    }

    public static CollectorTemplate<Ignore> minimalCheckstyleIgnore() {
        return CollectorTemplate.of(Ignore.class)
                .because("I don't agree", In.everywhere()
                        .ignore("import.avoidStar", "custom.import.order.nonGroup.expected", "custom.import.order.lex",
                                "javadoc.packageInfo", "javadoc.missing",
                                "multiple.variable.declarations.comma", "final.parameter",
                                "design.forExtension", "hidden.field", "inline.conditional.avoid", "magic.number"));
    }

    public static StyleChecks adjustedGoogleStyleChecks() {
        return StyleChecks.google()
                .maxLineLen(120).indentBasic(4).indentCase(4)
                .paramName("^[a-z][a-zA-Z0-9]*$")
                .catchParamName("^[a-z][a-zA-Z0-9]*$")
                .localVarName("^[a-z][a-zA-Z0-9]*$")
                .emptyLineSeparatorTokens(IMPORT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF,
                        STATIC_INIT, INSTANCE_INIT, METHOD_DEF, CTOR_DEF, VARIABLE_DEF);
    }

    public static StyleChecks adjustedSunStyleChecks() {
        return StyleChecks.sun().maxLineLen(120).allowDefaultAccessMembers(true);
    }
}
