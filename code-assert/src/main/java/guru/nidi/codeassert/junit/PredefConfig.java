package guru.nidi.codeassert.junit;

import guru.nidi.codeassert.config.CollectorTemplate;
import guru.nidi.codeassert.config.Ignore;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.pmd.CpdMatchCollector;
import guru.nidi.codeassert.pmd.PmdViolationCollector;
import guru.nidi.codeassert.pmd.Ruleset;
import guru.nidi.codeassert.pmd.Rulesets;

import static guru.nidi.codeassert.pmd.Rulesets.*;

public final class PredefConfig {
    private PredefConfig() {
    }

    public static CollectorTemplate<Ignore> minimalPmdIgnore() {
        return CollectorTemplate.forA(PmdViolationCollector.class)
                .because("junit", In.loc("*Test")
                        .ignore("JUnitSpelling", "JUnitAssertionsShouldIncludeMessage", "AvoidDuplicateLiterals",
                                "SignatureDeclareThrowsException", "TooManyStaticImports"))
                .because("I don't agree", In.everywhere()
                        .ignore("MethodArgumentCouldBeFinal", "AvoidFieldNameMatchingMethodName",
                                "CommentDefaultAccessModifier", "AbstractNaming", "AvoidFieldNameMatchingTypeName",
                                "UncommentedEmptyConstructor", "UseStringBufferForStringAppends",
                                "UncommentedEmptyMethodBody", "EmptyMethodInAbstractClassShouldBeAbstract"));
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
                comments().maxLines(35).maxLineLen(100).requirement(Rulesets.Comments.Requirement.Ignored),
                codesize().excessiveMethodLength(40).tooManyMethods(30),
                design(), empty().allowCommentedEmptyCatch(true), exceptions(), imports(), junit(),
                naming().variableLen(1, 20).methodLen(2),
                optimizations(), strings(),
                sunSecure(), typeResolution(), unnecessary(), unused()};
    }

}
