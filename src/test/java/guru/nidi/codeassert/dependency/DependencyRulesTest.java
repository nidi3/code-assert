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
package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.LocationMatcher;
import guru.nidi.codeassert.model.ModelAnalyzer;
import guru.nidi.codeassert.model.ModelResult;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
import static org.junit.Assert.*;

/**
 *
 */
public class DependencyRulesTest {
    private static final String CODE_ASSERT = "guru.nidi.codeassert.";
    private static final String DEP = CODE_ASSERT + "dependency.";
    private static final Set<String> WILDCARD_UNDEFINED = set("guru.nidi.codeassert", ca("config"), ca("model"), ca("dependency"), ca("junit"), dep("a"), dep("b"), dep("c"));
    private static final Set<String> UNDEFINED = set("guru.nidi.codeassert", ca("config"), ca("dependency"), ca("junit"), ca("model"), dep("a.a"), dep("a.b"), dep("b.a"), dep("b.b"), dep("c.a"), dep("c.b"));

    private ModelResult model;

    @Before
    public void analyze() {
        final ModelAnalyzer analyzer = new ModelAnalyzer(
                AnalyzerConfig.maven().mainAndTest("guru/nidi/codeassert/dependency"));
        model = analyzer.analyze();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wildcardNotAtEnd() {
        DependencyRule.allowAll("a*b");
    }

    //TODO test AmbigousRuleException

    @Test
    public void matcherFlags() {
        final DependencyRules rules = DependencyRules.allowAll();
        rules.addExternal("java.*");
        rules.addExternal("org.hamcrest*");
        rules.addRule(dep("a"));
        rules.addRule(dep("d"));
        final Set<String> undefined = new TreeSet<>(UNDEFINED);
        undefined.addAll(set("org.junit", dep("b"), dep("c")));

        final RuleResult result = rules.analyzeRules(model.findings());
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap(),
                        new DependencyMap(),
                        patterns(dep("d")),
                        undefined),
                result);

        assertThat(model, matchesRules(rules));

        assertMatcher("\nDefined, but not existing packages:\n" +
                        "guru.nidi.codeassert.dependency.d\n" +
                        "\nFound packages which are not defined:\n" +
                        "guru.nidi.codeassert, guru.nidi.codeassert.config, guru.nidi.codeassert.dependency, " +
                        "guru.nidi.codeassert.dependency.a.a, guru.nidi.codeassert.dependency.a.b, " +
                        "guru.nidi.codeassert.dependency.b, guru.nidi.codeassert.dependency.b.a, " +
                        "guru.nidi.codeassert.dependency.b.b, guru.nidi.codeassert.dependency.c, " +
                        "guru.nidi.codeassert.dependency.c.a, guru.nidi.codeassert.dependency.c.b, " +
                        "guru.nidi.codeassert.junit, guru.nidi.codeassert.model, org.junit\n",
                matchesExactly(rules));

        assertMatcher("\nFound packages which are not defined:\n" +
                        "guru.nidi.codeassert, guru.nidi.codeassert.config, guru.nidi.codeassert.dependency, " +
                        "guru.nidi.codeassert.dependency.a.a, guru.nidi.codeassert.dependency.a.b, " +
                        "guru.nidi.codeassert.dependency.b, guru.nidi.codeassert.dependency.b.a, " +
                        "guru.nidi.codeassert.dependency.b.b, guru.nidi.codeassert.dependency.c, " +
                        "guru.nidi.codeassert.dependency.c.a, guru.nidi.codeassert.dependency.c.b, " +
                        "guru.nidi.codeassert.junit, guru.nidi.codeassert.model, org.junit\n",
                matchesIgnoringNonExisting(rules));

        assertMatcher("\nDefined, but not existing packages:\n" +
                        "guru.nidi.codeassert.dependency.d\n",
                matchesIgnoringUndefined(rules));
    }

    @Test
    public void allow() {
        final DependencyRules rules = DependencyRules.allowAll();
        final DependencyRule a = rules.addRule(dep("a"));
        final DependencyRule b = rules.addRule(dep("b"));
        final DependencyRule c = rules.addRule(dep("c"));
        rules.addExternal("java.*");
        rules.addExternal("org*");

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
                .withRules(new GuruNidiCodeassertDependency());

        final RuleResult result = rules.analyzeRules(model.findings());
        assertEquals(result, rules2.analyzeRules(model.findings()));
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap().with(0, dep("a"), set(), dep("b")),
                        new DependencyMap().with(0, dep("b"), set(dep("b.B1")), dep("c")),
                        patterns(),
                        UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.b ->\n" +
                        "  guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.b.B1)\n",
                matchesRules(rules));
    }

    @Test
    public void deny() {
        final DependencyRules rules = DependencyRules.denyAll();
        final DependencyRule a = rules.addRule(dep("a"));
        final DependencyRule b = rules.addRule(dep("b"));
        final DependencyRule c = rules.addRule(dep("c"));
        rules.addExternal("java*");
        rules.addExternal("org*");

        a.mustUse(b);
        b.mayUse(c);

        final RuleResult result = rules.analyzeRules(model.findings());
        assertEquals(new RuleResult(
                        new DependencyMap(),
                        new DependencyMap().with(0, dep("a"), set(), dep("b")),
                        new DependencyMap()
                                .with(0, dep("a"), set(dep("a.A1")), dep("c"))
                                .with(0, dep("b"), set(dep("b.B1")), dep("a"))
                                .with(0, dep("c"), set(dep("c.C1")), dep("a"))
                                .with(0, dep("c"), set(dep("c.C1"), dep("c.C2")), dep("b")),
                        patterns(),
                        UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a ->\n" +
                        "  guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.a.A1)\n" +
                        "guru.nidi.codeassert.dependency.b ->\n" +
                        "  guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.b.B1)\n" +
                        "guru.nidi.codeassert.dependency.c ->\n" +
                        "  guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.c.C1)\n" +
                        "  guru.nidi.codeassert.dependency.b (by guru.nidi.codeassert.dependency.c.C1, guru.nidi.codeassert.dependency.c.C2)\n",
                matchesRules(rules));
    }

    @Test
    public void allowWithWildcard() {
        final DependencyRules rules = DependencyRules.allowAll();
        final DependencyRule a1 = rules.addRule(dep("a.a"));
        final DependencyRule a = rules.addRule(dep("a.*"));
        final DependencyRule b = rules.addRule(dep("b.*"));
        final DependencyRule c = rules.addRule(dep("c.*"));
        rules.addExternal("java*");
        rules.addExternal("org*");

        a.mustUse(b);
        b.mustNotUse(a, c).mayUse(a1);

        final RuleResult result = rules.analyzeRules(model.findings());
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

        assertEquals(result, rules2.analyzeRules(model.findings()));
        assertEquals(new RuleResult(
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
                        WILDCARD_UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "guru.nidi.codeassert.dependency.a.b ->\n" +
                        "  guru.nidi.codeassert.dependency.b.a\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.b.a ->\n" +
                        "  guru.nidi.codeassert.dependency.a.b (by guru.nidi.codeassert.dependency.b.a.Ba1)\n" +
                        "  guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n" +
                        "  guru.nidi.codeassert.dependency.c.b (by guru.nidi.codeassert.dependency.b.a.Ba2)\n" +
                        "guru.nidi.codeassert.dependency.b.b ->\n" +
                        "  guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.b.Bb1)\n" +
                        "  guru.nidi.codeassert.dependency.c.b (by guru.nidi.codeassert.dependency.b.b.Bb1)\n",
                matchesRules(rules));
    }

    @Test
    public void denyWithWildcard() {
        final DependencyRules rules = DependencyRules.denyAll();
        final DependencyRule a1 = rules.addRule(dep("a.a"));
        final DependencyRule a = rules.addRule(dep("a.*"));
        final DependencyRule b = rules.addRule(dep("b.*"));
        final DependencyRule c = rules.addRule(dep("c.*"));
        rules.addExternal("java.*");
        rules.addExternal("org*");

        a.mustUse(b);
        b.mayUse(a, c).mustNotUse(a1);

        final RuleResult result = rules.analyzeRules(model.findings());
        assertEquals(new RuleResult(
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
                        WILDCARD_UNDEFINED),
                result);
        assertMatcher("\n" +
                        "Found missing dependencies:\n" +
                        "guru.nidi.codeassert.dependency.a.a ->\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "guru.nidi.codeassert.dependency.a.b ->\n" +
                        "  guru.nidi.codeassert.dependency.b.a\n" +
                        "  guru.nidi.codeassert.dependency.b.b\n" +
                        "\n" +
                        "Found forbidden dependencies:\n" +
                        "guru.nidi.codeassert.dependency.b.a ->\n" +
                        "  guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n" +
                        "guru.nidi.codeassert.dependency.c.a ->\n" +
                        "  guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n" +
                        "  guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n",
                matchesRules(rules));
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

    private void assertMatcher(String message, Matcher<ModelResult> matcher) {
        assertFalse(matcher.matches(model));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(model, sd);
        assertEquals(message, sd.toString());
    }
}
