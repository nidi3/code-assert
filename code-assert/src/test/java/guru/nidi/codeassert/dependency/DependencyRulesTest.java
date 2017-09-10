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
package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.model.Model;
import guru.nidi.codeassert.model.Scope;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class DependencyRulesTest {
    private static final String CODE_ASSERT = "guru.nidi.codeassert.";
    private static final String DEP = CODE_ASSERT + "dependency.";
    private static final Set<String> WILDCARD_UNDEFINED = set("guru.nidi.codeassert", ca("config"), ca("dependency"), ca("model"), ca("util"), ca("junit"), dep("a"), dep("b"), dep("c"));
    private static final Set<String> UNDEFINED = set("guru.nidi.codeassert", ca("config"), ca("dependency"), ca("junit"), ca("model"), ca("util"), dep("a.a"), dep("a.b"), dep("b.a"), dep("b.b"), dep("c.a"), dep("c.b"));
    private static final Set<DependencyMap> CYCLES = new HashSet<>(asList(new DependencyMap()
                    .with(1, dep("a.a"), set(dep("a.a.Aa1")), dep("b.a"))
                    .with(1, dep("b.a"), set(dep("b.a.Ba1")), dep("a.a"))
                    .with(1, dep("b.a"), set(dep("b.a.Ba2")), dep("c.a"))
                    .with(1, dep("c.a"), set(dep("c.a.Ca1")), dep("a.a"))
                    .with(1, dep("c.a"), set(dep("c.a.Ca1")), dep("b.a")),
            new DependencyMap()
                    .with(1, dep("c"), set(dep("c.C2"), dep("c.C1")), dep("b"))
                    .with(1, dep("c"), set(dep("c.C1")), dep("a"))
                    .with(1, dep("b"), set(dep("b.B1")), dep("c"))
                    .with(1, dep("b"), set(dep("b.B1")), dep("a"))
                    .with(1, dep("a"), set(dep("a.A1")), dep("c"))));

    private Model model;

    @Before
    public void analyze() {
        model = Model.from(AnalyzerConfig.maven().mainAndTest("guru/nidi/codeassert/dependency").getClasses());
    }

    @Test(expected = IllegalArgumentException.class)
    public void wildcardNotAtEnd() {
        DependencyRule.allowAll("a*b");
    }

    //TODO test AmbiguousRuleException
    /*
    @Test
    public void classes() {
        final DependencyRules rules = DependencyRules.denyAll();
        rules.withExternals("java.*", "org*");
        class Rules extends DependencyRuler{
            DependencyRule $A1,$A2,$B1,$X,guruNidiCodeassert_;

            @Override
            public void defineRules() {
                B1.mustUse(A2).mustNotUse(A1);
                guruNidiCodeassert_.mayUse(guruNidiCodeassert_);
            }
        }
        final DependencyRule a2 = rules.addRule("A2");
        final DependencyRule a1 = rules.addRule("A1");
        rules.addRule("B1").mustUse(a2).mustNotUse(a1);
        rules.addRule("X");
        final DependencyRule all = rules.addRule(ca("*"));
        all.mayUse(all);
        rules.withAbsoluteRules(new Rules());
        final RuleResult result = rules.analyzeRules(model.findings().classView());
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap().with(0, dep("b.B1"), set(), dep("a.A2")),
                        new DependencyMap()
                                .with(0, dep("CycleTest"), set(), ca("AnalyzerResult"))
                                .with(0, dep("b.B1"), set(), dep("a.A1")),
                        patterns("X"),
                        set(ca("AnalyzerResult"))),
                result);
    }
    */

    @Test
    public void matcherFlags() {
        final DependencyRules rules = DependencyRules.allowAll().withExternals("java.*", "org.hamcrest*");
        rules.addRule(dep("a"));
        rules.addRule(dep("d"));
        final Set<String> undefined = new TreeSet<>(UNDEFINED);
        undefined.addAll(set("org.junit", "org.slf4j", dep("b"), dep("c")));

        final Dependencies result = rules.analyzeRules(Scope.packages(model));
        assertEquals(new Dependencies(
                        new DependencyMap(),
                        new DependencyMap(),
                        new DependencyMap(),
                        patterns(dep("d")),
                        undefined,
                        CYCLES),
                result);

        assertThat(new DependencyAnalyzer(model).rules(rules).analyze(), matchesRules());

        assertMatcher(""
                        + "NOT_EXISTING guru.nidi.codeassert.dependency.d\n"
                        + "UNDEFINED    guru.nidi.codeassert\n"
                        + "UNDEFINED    guru.nidi.codeassert.config\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.a.a\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.a.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.b.a\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.b.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.c\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.c.a\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.c.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.junit\n"
                        + "UNDEFINED    guru.nidi.codeassert.model\n"
                        + "UNDEFINED    guru.nidi.codeassert.util\n"
                        + "UNDEFINED    org.junit\n"
                        + "UNDEFINED    org.slf4j\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRulesExactly());

        assertMatcher(""
                        + "UNDEFINED    guru.nidi.codeassert\n"
                        + "UNDEFINED    guru.nidi.codeassert.config\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.a.a\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.a.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.b.a\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.b.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.c\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.c.a\n"
                        + "UNDEFINED    guru.nidi.codeassert.dependency.c.b\n"
                        + "UNDEFINED    guru.nidi.codeassert.junit\n"
                        + "UNDEFINED    guru.nidi.codeassert.model\n"
                        + "UNDEFINED    guru.nidi.codeassert.util\n"
                        + "UNDEFINED    org.junit\n"
                        + "UNDEFINED    org.slf4j\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRulesIgnoringNonExisting());

        assertMatcher("NOT_EXISTING guru.nidi.codeassert.dependency.d\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRulesIgnoringUndefined());
    }

    @Test
    public void allow() {
        final DependencyRules rules = DependencyRules.allowAll().withExternals("java.*", "org*");
        final DependencyRule a = rules.addRule(dep("a"));
        final DependencyRule b = rules.addRule(dep("b"));
        final DependencyRule c = rules.addRule(dep("c"));

        a.mustUse(b);
        b.mustNotUse(c);

        class GuruNidiCodeassertDependency extends DependencyRuler {
            DependencyRule a, b, c;

            public void defineRules() {
                a.mustUse(b);
                b.mustNotUse(c);
            }
        }

        final DependencyRules rules2 = DependencyRules.allowAll()
                .withExternals(new DependencyRuler() {
                    DependencyRule java_, org_;
                })
                .withRelativeRules(new GuruNidiCodeassertDependency());

        final Dependencies result = rules.analyzeRules(Scope.packages(model));
        assertEquals(result, rules2.analyzeRules(Scope.packages(model)));
        assertEquals(new Dependencies(
                        new DependencyMap(),
                        new DependencyMap().with(0, dep("a"), set(), dep("b")),
                        new DependencyMap().with(0, dep("b"), set(dep("b.B1")), dep("c")),
                        patterns(),
                        UNDEFINED,
                        CYCLES),
                result);
        assertMatcher(""
                        + "MISSING      guru.nidi.codeassert.dependency.a ->\n"
                        + "  guru.nidi.codeassert.dependency.b\n"
                        + "DENIED       guru.nidi.codeassert.dependency.b ->\n"
                        + "  guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.b.B1)\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRules());
    }

    @Test
    public void deny() {
        final DependencyRules rules = DependencyRules.denyAll().withExternals("java*", "org*");
        final DependencyRule a = rules.addRule(dep("a"));
        final DependencyRule b = rules.addRule(dep("b"));
        final DependencyRule c = rules.addRule(dep("c"));

        a.mustUse(b);
        b.mayUse(c);

        final Dependencies result = rules.analyzeRules(Scope.packages(model));
        assertEquals(new Dependencies(
                        new DependencyMap(),
                        new DependencyMap().with(0, dep("a"), set(), dep("b")),
                        new DependencyMap()
                                .with(0, dep("a"), set(dep("a.A1")), dep("c"))
                                .with(0, dep("b"), set(dep("b.B1")), dep("a"))
                                .with(0, dep("c"), set(dep("c.C1")), dep("a"))
                                .with(0, dep("c"), set(dep("c.C1"), dep("c.C2")), dep("b")),
                        patterns(),
                        UNDEFINED,
                        CYCLES),
                result);
        assertMatcher(""
                        + "MISSING      guru.nidi.codeassert.dependency.a ->\n"
                        + "  guru.nidi.codeassert.dependency.b\n"
                        + "DENIED       guru.nidi.codeassert.dependency.a ->\n"
                        + "  guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.a.A1)\n"
                        + "DENIED       guru.nidi.codeassert.dependency.b ->\n"
                        + "  guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.b.B1)\n"
                        + "DENIED       guru.nidi.codeassert.dependency.c ->\n"
                        + "  guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.c.C1)\n"
                        + "  guru.nidi.codeassert.dependency.b (by guru.nidi.codeassert.dependency.c.C1, guru.nidi.codeassert.dependency.c.C2)\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRules());
    }

    @Test
    public void allowWithWildcard() {
        final DependencyRules rules = DependencyRules.allowAll().withExternals("java*", "org*");
        final DependencyRule a1 = rules.addRule(dep("a.a"));
        final DependencyRule a = rules.addRule(dep("a.*"));
        final DependencyRule b = rules.addRule(dep("b.*"));
        final DependencyRule c = rules.addRule(dep("c.*"));

        a.mustUse(b);
        b.mustNotUse(a, c).mayUse(a1);

        final Dependencies result = rules.analyzeRules(Scope.packages(model));
        final DependencyRules rules2 = DependencyRules.allowAll()
                .withRules("guru.nidi.codeassert.dependency", new DependencyRuler() {
                    DependencyRule aA, a_, b_, c_;

                    @Override
                    public void defineRules() {
                        a_.mustUse(b_);
                        b_.mustNotUse(a_, c_).mayUse(aA);
                    }
                })
                .withExternals(new DependencyRuler() {
                    DependencyRule java_, org_;
                });

        assertEquals(result, rules2.analyzeRules(Scope.packages(model)));
        assertEquals(new Dependencies(
                        new DependencyMap(),
                        new DependencyMap()
                                .with(0, dep("a.a"), set(), dep("b.b"))
                                .with(0, dep("a.b"), set(), dep("b.a"))
                                .with(0, dep("a.b"), set(), dep("b.b")),
                        new DependencyMap()
                                .with(0, dep("b.a"), set(dep("b.a.Ba1")), dep("a.b"))
                                .with(0, dep("b.a"), set(dep("b.a.Ba2")), dep("c.b"))
                                .with(0, dep("b.a"), set(dep("b.a.Ba2")), dep("c.a"))
                                .with(0, dep("b.b"), set(dep("b.b.Bb1")), dep("c.a"))
                                .with(0, dep("b.b"), set(dep("b.b.Bb1")), dep("c.b")),
                        patterns(),
                        WILDCARD_UNDEFINED,
                        CYCLES),
                result);
        assertMatcher(""
                        + "MISSING      guru.nidi.codeassert.dependency.a.a ->\n"
                        + "  guru.nidi.codeassert.dependency.b.b\n"
                        + "MISSING      guru.nidi.codeassert.dependency.a.b ->\n"
                        + "  guru.nidi.codeassert.dependency.b.a\n"
                        + "  guru.nidi.codeassert.dependency.b.b\n"
                        + "DENIED       guru.nidi.codeassert.dependency.b.a ->\n"
                        + "  guru.nidi.codeassert.dependency.a.b (by guru.nidi.codeassert.dependency.b.a.Ba1)\n"
                        + "  guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n"
                        + "  guru.nidi.codeassert.dependency.c.b (by guru.nidi.codeassert.dependency.b.a.Ba2)\n"
                        + "DENIED       guru.nidi.codeassert.dependency.b.b ->\n"
                        + "  guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.b.Bb1)\n"
                        + "  guru.nidi.codeassert.dependency.c.b (by guru.nidi.codeassert.dependency.b.b.Bb1)\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRules());
    }

    @Test
    public void denyWithWildcard() {
        final DependencyRules rules = DependencyRules.denyAll().withExternals("java.*", "org*");
        final DependencyRule a1 = rules.addRule(dep("a.a"));
        final DependencyRule a = rules.addRule(dep("a.*"));
        final DependencyRule b = rules.addRule(dep("b.*"));
        final DependencyRule c = rules.addRule(dep("c.*"));

        a.mustUse(b);
        b.mayUse(a, c).mustNotUse(a1);

        final Dependencies result = rules.analyzeRules(Scope.packages(model));
        assertEquals(new Dependencies(
                        new DependencyMap(),
                        new DependencyMap()
                                .with(0, dep("a.a"), set(), dep("b.b"))
                                .with(0, dep("a.b"), set(), dep("b.a"))
                                .with(0, dep("a.b"), set(), dep("b.b")),
                        new DependencyMap()
                                .with(0, dep("b.a"), set(dep("b.a.Ba1")), dep("a.a"))
                                .with(0, dep("c.a"), set(dep("c.a.Ca1")), dep("a.a"))
                                .with(0, dep("c.a"), set(dep("c.a.Ca1")), dep("b.a")),
                        patterns(),
                        WILDCARD_UNDEFINED,
                        CYCLES),
                result);
        assertMatcher(""
                        + "MISSING      guru.nidi.codeassert.dependency.a.a ->\n"
                        + "  guru.nidi.codeassert.dependency.b.b\n"
                        + "MISSING      guru.nidi.codeassert.dependency.a.b ->\n"
                        + "  guru.nidi.codeassert.dependency.b.a\n"
                        + "  guru.nidi.codeassert.dependency.b.b\n"
                        + "DENIED       guru.nidi.codeassert.dependency.b.a ->\n"
                        + "  guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n"
                        + "DENIED       guru.nidi.codeassert.dependency.c.a ->\n"
                        + "  guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n"
                        + "  guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n",
                new DependencyAnalyzer(model).rules(rules).analyze(), matchesRules());
    }

    @Test
    public void externalsAreOptional() {
        final DependencyRules rules = DependencyRules.denyAll().withExternals("java*", "org.*", "blablu");
        final DependencyRule all = rules.addRule("guru.nidi.codeassert*");
        all.mayUse(all);
        assertThat(new DependencyAnalyzer(model).rules(rules).analyze(), matchesRulesExactly());
    }

    @Test
    public void collector() {
        final DependencyCollector collector = new DependencyCollector()
                .just(In.locs(dep("b*"), ca("x"), ca("dependency")).ignoreAll())
                .just(In.locs(ca("z")).ignoreAll())
                .just(In.loc("guru.nidi.codeassert").ignore(DependencyCollector.UNDEFINED))
                .just(In.loc(dep("c*")).ignore(DependencyCollector.DENIED));
        final AnalyzerConfig config = AnalyzerConfig.maven().mainAndTest("guru/nidi/codeassert/dependency");
        final DependencyRules rules = DependencyRules.denyAll().withExternals("java*", "org.*");
        rules.addRule(ca("*"));
        final DependencyRule a = rules.addRule(dep("a"));
        rules.addRule(dep("b.b")).mustUse(a);
        rules.addRule(ca("x"));
        rules.addRule(ca("y"));

        final DependencyResult result = new DependencyAnalyzer(config).rules(rules).collector(collector).analyze();
        assertEquals(new Dependencies(
                        new DependencyMap(),
                        new DependencyMap(),
                        new DependencyMap()
                                .with(0, dep("a.a"), set(dep("a.a.Aa1")), dep("b.a"))
                                .with(0, dep("a"), set(dep("a.A1")), dep("c")),
                        new HashSet<>(asList(new LocationMatcher("guru.nidi.codeassert.y"))),
                        new HashSet<String>(),
                        new HashSet<>(asList(
                                new DependencyMap()
                                        .with(1, dep("c.a"), set(dep("c.a.Ca1")), dep("a.a")),
                                new DependencyMap()
                                        .with(1, dep("c"), set(dep("c.C1")), dep("a"))
                                        .with(1, dep("a"), set(dep("a.A1")), dep("c"))))),
                result.findings());
        assertEquals(asList("    ignore all in [guru.nidi.codeassert.z]"), result.unusedActions());
    }

    @Test
    public void classLevel() {
        final DependencyRules rules = DependencyRules.denyAll().withExternals("java.*", "org*");
        final DependencyRule m = rules.rule(ca("model"));
        final DependencyRule c = rules.rule(ca("config"));
        final DependencyRule a = rules.addRule(dep("CycleTest"));
        a.mayUse(m, c);

        final DependencyRules rules2 = DependencyRules.denyAll()
                .withRules("guru.nidi.codeassert", new DependencyRuler() {
                    JavaElement model, config, dependency;

                    @Override
                    public void defineRules() {
                        dependency.sub("CycleTest").mayUse(model, config);
                    }
                })
                .withExternals("java.*", "org*");

        final Dependencies result = rules.analyzeRules(Scope.classes(model));
        final Dependencies result2 = rules2.analyzeRules(Scope.classes(model));
        final Dependencies result3 = rules.allowIntraPackageDependencies(true).analyzeRules(Scope.classes(model));
        assertEquals(result, result2);
        assertEquals(new DependencyMap()
                        .with(0, dep("CycleTest"), set(), dep("DependencyAnalyzer"))
                        .with(0, dep("CycleTest"), set(), dep("DependencyResult"))
                        .with(0, dep("CycleTest"), set(), dep("DependencyCollector"))
                        .with(0, dep("CycleTest"), set(), ca("junit.CodeAssertMatchers")),
                result.denied);
        assertEquals(new DependencyMap()
                        .with(0, dep("CycleTest"), set(), ca("junit.CodeAssertMatchers")),
                result3.denied);
        assertEquals(67, result.undefined.size());
    }

    private static String ca(String s) {
        return CODE_ASSERT + s;
    }

    private static String dep(String s) {
        return DEP + s;
    }

    private static Set<String> set(String... ss) {
        final Set<String> res = new TreeSet<>();
        Collections.addAll(res, ss);
        return res;
    }

    private static Set<LocationMatcher> patterns(String... ss) {
        final Set<LocationMatcher> res = new TreeSet<>();
        for (final String s : ss) {
            res.add(new LocationMatcher(s));
        }
        return res;
    }

    private void assertMatcher(String message, DependencyResult result, Matcher<DependencyResult> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertEquals(message, sd.toString());
    }
}
