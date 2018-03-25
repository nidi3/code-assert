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
package guru.nidi.codeassert;

import guru.nidi.codeassert.checkstyle.*;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.findbugs.*;
import guru.nidi.codeassert.jacoco.Coverage;
import guru.nidi.codeassert.junit.CodeAssertJunit5Test;
import guru.nidi.codeassert.junit.PredefConfig;
import guru.nidi.codeassert.pmd.*;
import net.sourceforge.pmd.RulePriority;
import org.junit.jupiter.api.BeforeAll;

import java.util.Locale;

import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;

public class EatYourOwnDogfoodTest extends CodeAssertJunit5Test {
    @BeforeAll
    static void init() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Override
    protected DependencyResult analyzeDependencies() {
        class GuruNidiCodeassert extends DependencyRuler {
            DependencyRule config, dependency, findbugs, checkstyle, detekt, model, pmd, ktlint, util, junit, junitKotlin, jacoco;

            @Override
            public void defineRules() {
                base().mayBeUsedBy(all());
                config.mayBeUsedBy(all());
                util.mayBeUsedBy(all());
                dependency.mayUse(model);
                junit.mayUse(model, dependency, findbugs, checkstyle, pmd, jacoco);
                junitKotlin.mayUse(ktlint, detekt);
            }
        }

        final DependencyRules rules = denyAll()
                .withExternals("edu.*", "java.*", "net.*", "org.*", "com.*", "kotlin.*", "io.*")
                .withRelativeRules(new GuruNidiCodeassert());
        return new DependencyAnalyzer(AnalyzerConfig.maven().main()).rules(rules).analyze();
    }

    @Override
    protected FindBugsResult analyzeFindBugs() {
        System.gc();
        final BugCollector bugCollector = new BugCollector()
                .apply(PredefConfig.minimalFindBugsIgnore())
                .just(
                        In.clazz(DependencyRules.class).withMethods("withRules").and(In.clazz(PmdRuleset.class)).ignore("DP_DO_INSIDE_DO_PRIVILEGED"),
                        In.classes("*Comparator").ignore("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"),
                        In.classes("*Exception").ignore("SE_BAD_FIELD"),
                        In.clazz(Coverage.class).ignore("EQ_COMPARETO_USE_OBJECT_EQUALS"),
                        In.everywhere().ignore("EI_EXPOSE_REP", "EI_EXPOSE_REP2", "PATH_TRAVERSAL_IN", "CRLF_INJECTION_LOGS"),
                        In.classes("ClassFileParser", "Constant", "MemberInfo", "PmdRulesets", "Reason").ignore("URF_UNREAD_FIELD"));
        return new FindBugsAnalyzer(AnalyzerConfig.maven().main(), bugCollector).analyze();
    }

    @Override
    protected PmdResult analyzePmd() {
        System.gc();
        final PmdViolationCollector collector = new PmdViolationCollector().minPriority(RulePriority.MEDIUM)
                .apply(PredefConfig.minimalPmdIgnore())
                .just(
                        In.everywhere().ignore(
                                "AvoidInstantiatingObjectsInLoops", "AvoidSynchronizedAtMethodLevel",
                                "SimplifyStartsWith", "ArrayIsStoredDirectly", "MethodReturnsInternalArray"),
                        In.classes("AttributeInfo", "ConstantPool").ignore("ArrayIsStoredDirectly"),
                        In.classes("SignatureParser").ignore("SwitchStmtsShouldHaveDefault"),
                        In.clazz(PmdRulesets.class).ignore("TooManyMethods", "AvoidDuplicateLiterals"),
                        In.classes("Reason").ignore("SingularField"),
                        In.clazz(Coverage.class).ignore("ExcessiveParameterList"),
                        In.clazz(LocationMatcher.class).ignore("GodClass"),
                        In.classes("SourceFileParser", "Location", "LocationMatcher")
                                .ignore("CyclomaticComplexity", "ModifiedCyclomaticComplexity", "StdCyclomaticComplexity"),
                        In.classes("DependencyRules", "CodeClassBuilder").ignore("GodClass"));
        return new PmdAnalyzer(AnalyzerConfig.maven().main(), collector)
                .withRulesets(PredefConfig.defaultPmdRulesets())
                .analyze();
    }

    @Override
    protected CpdResult analyzeCpd() {
        System.gc();
        final CpdMatchCollector collector = new CpdMatchCollector()
                .apply(PredefConfig.cpdIgnoreEqualsHashCodeToString())
                .just(In.classes("DetektMatcher", "ProjectLayout", "SourceFileParser").ignoreAll())
                .just(In.clazz(PmdAnalyzer.class).ignore("Map<String, PmdRuleset> newRuleset"));

        return new CpdAnalyzer(AnalyzerConfig.maven().main(), 27, collector).analyze();
    }

    @Override
    protected CheckstyleResult analyzeCheckstyle() {
        System.gc();
        final StyleEventCollector collector = new StyleEventCollector()
                .apply(PredefConfig.minimalCheckstyleIgnore())
                .just(In.classes("Coverage", "Constant").ignore("empty.line.separator"))
                .just(In.classes("BaseCollector", "In", "Location", "SourceFileParser").ignore("overload.methods.declaration"))
                .just(In.clazz(PmdRulesets.class).ignore("abbreviation.as.word"))
                .just(In.classes("SignatureParser").ignore("name.invalidPattern"))
                .just(In.classes("DependencyMap").ignore("tag.continuation.indent"));

        return new CheckstyleAnalyzer(AnalyzerConfig.maven().main(), PredefConfig.adjustedGoogleStyleChecks(), collector).analyze();
    }
}
