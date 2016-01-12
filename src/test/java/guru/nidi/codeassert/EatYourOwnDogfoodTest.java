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
package guru.nidi.codeassert;

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.dependency.DependencyRule;
import guru.nidi.codeassert.dependency.DependencyRuler;
import guru.nidi.codeassert.dependency.DependencyRules;
import guru.nidi.codeassert.findbugs.BugCollector;
import guru.nidi.codeassert.findbugs.FindBugsAnalyzer;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.junit.CodeAssertTest;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.model.ModelResult;
import guru.nidi.codeassert.pmd.PmdAnalyzer;
import guru.nidi.codeassert.pmd.PmdResult;
import guru.nidi.codeassert.pmd.Rulesets;
import guru.nidi.codeassert.pmd.ViolationCollector;
import guru.nidi.codeassert.util.LocationMatcher;
import net.sourceforge.pmd.RulePriority;
import org.junit.Test;

import java.util.EnumSet;

import static guru.nidi.codeassert.dependency.DependencyMatchers.matchesExactly;
import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.junit.CodeAssertTest.Type.*;
import static guru.nidi.codeassert.pmd.Rulesets.*;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EatYourOwnDogfoodTest extends CodeAssertTest {
    @Override
    protected EnumSet<Type> defaultTests() {
        return EnumSet.of(CIRCULAR_DEPENDENCIES, PMD, PMD_UNUSED_ACTIONS, FIND_BUGS, FIND_BUGS_UNUSED_ACTIONS);
    }

    @Test
    public void dependency() {
        class GuruNidiCodeassert implements DependencyRuler {
            DependencyRule $self, config, dependency, findbugs, model, pmd, util, junit;

            @Override
            public void defineRules() {
                config.mayUse(util);
                dependency.mayUse($self, util, config, model);
                findbugs.mayUse($self, util, config);
                model.mayUse($self, util, config);
                pmd.mayUse($self, util, config);
                util.mayUse($self);
                junit.mayUse(model, dependency, findbugs, pmd);
            }
        }
        final DependencyRules rules = denyAll().withExternals("edu*", "java*", "net*", "org*").withRules(new GuruNidiCodeassert());
        assertThat(modelResult(), matchesExactly(rules));
    }

    @Override
    protected ModelResult analyzeModel() {
        return new ModelAnalyzer(AnalyzerConfig.maven().main()).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        final BugCollector bugCollector = new BugCollector().just(
                In.locs("DependencyRules#withRules", "Ruleset").ignore("DP_DO_INSIDE_DO_PRIVILEGED"),
                In.loc("*Comparator").ignore("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"),
                In.loc("*Exception").ignore("SE_BAD_FIELD"),
                In.locs("ClassFileParser", "Constant", "MemberInfo", "Rulesets$*", "Reason").ignore("URF_UNREAD_FIELD"));
        return new FindBugsAnalyzer(AnalyzerConfig.maven().main(), bugCollector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        final ViolationCollector collector = new ViolationCollector().minPriority(RulePriority.MEDIUM).just(
                In.everywhere().ignore(
                        "MethodArgumentCouldBeFinal", "AvoidFieldNameMatchingMethodName",
                        "CommentDefaultAccessModifier", "AbstractNaming", "AvoidFieldNameMatchingTypeName",
                        "UncommentedEmptyConstructor", "AvoidInstantiatingObjectsInLoops",
                        "UseStringBufferForStringAppends", "AvoidSynchronizedAtMethodLevel",
                        "JUnitAssertionsShouldIncludeMessage"),
                In.locs("AttributeInfo", "ConstantPool").ignore("ArrayIsStoredDirectly"),
                In.loc("Rulesets$*").ignore("AvoidDuplicateLiterals"),
                In.loc("SignatureParser").ignore("SwitchStmtsShouldHaveDefault"),
                In.clazz(Rulesets.class).ignore("TooManyMethods"),
                In.clazz(LocationMatcher.class).ignore("SimplifyStartsWith"),
                In.loc("*Test").ignore("TooManyStaticImports"),
                In.loc("Reason").ignore("SingularField"),
                In.locs("DependencyRules", "JavaClassImportBuilder").ignore("GodClass"));
        return new PmdAnalyzer(AnalyzerConfig.maven().main(), collector)
                .withRuleSets(basic(), braces(),
                        comments().maxLines(35).maxLineLen(100).requirement(Comments.Requirement.Ignored),
                        codesize().excessiveMethodLength(40).tooManyMethods(30),
                        design(), empty(), exceptions(), imports(), junit(),
                        naming().variableLen(1, 20).methodLen(2),
                        optimizations(), strings(),
                        sunSecure(), typeResolution(), unnecessary(), unused())
                .analyze();
    }

}
