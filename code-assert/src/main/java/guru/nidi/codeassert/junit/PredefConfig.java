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
package guru.nidi.codeassert.junit;

import guru.nidi.codeassert.checkstyle.StyleChecks;
import guru.nidi.codeassert.config.CollectorTemplate;
import guru.nidi.codeassert.config.Ignore;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.pmd.CpdMatchCollector;
import guru.nidi.codeassert.pmd.PmdViolationCollector;
import guru.nidi.codeassert.pmd.Ruleset;
import guru.nidi.codeassert.pmd.Rulesets;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static guru.nidi.codeassert.pmd.Rulesets.*;

public final class PredefConfig {
    private PredefConfig() {
    }

    public static CollectorTemplate<Ignore> minimalPmdIgnore() {
        return CollectorTemplate.forA(PmdViolationCollector.class)
                .because("junit", In.locs("*Test", "Test*")
                        .ignore("JUnitSpelling", "JUnitAssertionsShouldIncludeMessage", "AvoidDuplicateLiterals",
                                "SignatureDeclareThrowsException", "TooManyStaticImports"))
                .because("I don't agree", In.everywhere()
                        .ignore("MethodArgumentCouldBeFinal", "AvoidFieldNameMatchingMethodName",
                                "CommentDefaultAccessModifier", "AbstractNaming", "AvoidFieldNameMatchingTypeName",
                                "UncommentedEmptyConstructor", "UseStringBufferForStringAppends",
                                "UncommentedEmptyMethodBody", "EmptyMethodInAbstractClassShouldBeAbstract"))
                .because("it's equals", In.loc("#equals")
                        .ignore("NPathComplexity", "ModifiedCyclomaticComplexity", "StdCyclomaticComplexity",
                                "CyclomaticComplexity", "ConfusingTernary"))
                .because("it's hashCode", In.loc("#hashCode")
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
                .because("modern compilers are clever",
                        In.everywhere().ignore("SBSC_USE_STRINGBUFFER_CONCATENATION"));
    }

    public static Ruleset[] defaultPmdRulesets() {
        return new Ruleset[]{
                basic(), braces(),
                comments().maxLines(35).maxLineLen(120).requirement(Rulesets.Comments.Requirement.Ignored),
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
