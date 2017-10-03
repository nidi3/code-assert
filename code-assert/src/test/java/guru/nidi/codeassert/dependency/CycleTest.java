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

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.model.Scope;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.dependency.DependencyCollector.CYCLE;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoCycles;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CycleTest {
    private static final String BASE = "guru.nidi.codeassert.dependency.";

    public DependencyResult analyze(Scope scope, DependencyCollector collector) {
        return new DependencyAnalyzer(AnalyzerConfig.maven().test("guru/nidi/codeassert/dependency")).scope(scope).collector(collector).analyze();
    }

    @Test
    void packageCycles() {
        assertMatcher("\n"
                        + "CYCLE        This group of elements has mutual dependencies:\n"
                        + "  guru.nidi.codeassert.dependency.a ->\n"
                        + "    guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.a.A1)\n"
                        + "  guru.nidi.codeassert.dependency.b ->\n"
                        + "    guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.b.B1)\n"
                        + "    guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.b.B1)\n"
                        + "  guru.nidi.codeassert.dependency.c ->\n"
                        + "    guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.c.C1)\n"
                        + "    guru.nidi.codeassert.dependency.b (by guru.nidi.codeassert.dependency.c.C1, guru.nidi.codeassert.dependency.c.C2)\n"
                        + "CYCLE        This group of elements has mutual dependencies:\n"
                        + "  guru.nidi.codeassert.dependency.a.a ->\n"
                        + "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.a.a.Aa1)\n"
                        + "  guru.nidi.codeassert.dependency.b.a ->\n"
                        + "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n"
                        + "    guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n"
                        + "  guru.nidi.codeassert.dependency.c.a ->\n"
                        + "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n"
                        + "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n",
                analyze(Scope.PACKAGES, new DependencyCollector()), hasNoCycles());
    }

    @Test
    void packageCyclesWithExceptions() {
        final DependencyCollector collector = new DependencyCollector()
                .just(In.locs(base("a"), base("b"), base("c")).ignore(CYCLE));
        assertMatcher("\n"
                        + "CYCLE        This group of elements has mutual dependencies:\n"
                        + "  guru.nidi.codeassert.dependency.a.a ->\n"
                        + "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.a.a.Aa1)\n"
                        + "  guru.nidi.codeassert.dependency.b.a ->\n"
                        + "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n"
                        + "    guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n"
                        + "  guru.nidi.codeassert.dependency.c.a ->\n"
                        + "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n"
                        + "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n",
                analyze(Scope.PACKAGES, collector), hasNoCycles());
    }

    @Test
    void classCycles() {
        assertMatcher("\n"
                        + "CYCLE        This group of elements has mutual dependencies:\n"
                        + "  guru.nidi.codeassert.dependency.a.A1 ->\n"
                        + "    guru.nidi.codeassert.dependency.c.C1\n"
                        + "  guru.nidi.codeassert.dependency.b.B1 ->\n"
                        + "    guru.nidi.codeassert.dependency.a.A1\n"
                        + "    guru.nidi.codeassert.dependency.c.C1\n"
                        + "  guru.nidi.codeassert.dependency.c.C1 ->\n"
                        + "    guru.nidi.codeassert.dependency.a.A1\n"
                        + "    guru.nidi.codeassert.dependency.b.B1\n"
                        + "CYCLE        This group of elements has mutual dependencies:\n"
                        + "  guru.nidi.codeassert.dependency.a.a.Aa1 ->\n"
                        + "    guru.nidi.codeassert.dependency.b.a.Ba1\n"
                        + "  guru.nidi.codeassert.dependency.b.a.Ba1 ->\n"
                        + "    guru.nidi.codeassert.dependency.a.a.Aa1\n",
                analyze(Scope.CLASSES, new DependencyCollector()), hasNoCycles());
    }

    @Test
    void classCyclesExcept() {
        final DependencyCollector collector = new DependencyCollector()
                .just(In.locs("guru.nidi.codeassert.dependency.a.a.Aa1", "guru.nidi.codeassert.dependency.b.a.Ba1").ignore(CYCLE));
        assertMatcher("\n"
                        + "CYCLE        This group of elements has mutual dependencies:\n"
                        + "  guru.nidi.codeassert.dependency.a.A1 ->\n"
                        + "    guru.nidi.codeassert.dependency.c.C1\n"
                        + "  guru.nidi.codeassert.dependency.b.B1 ->\n"
                        + "    guru.nidi.codeassert.dependency.a.A1\n"
                        + "    guru.nidi.codeassert.dependency.c.C1\n"
                        + "  guru.nidi.codeassert.dependency.c.C1 ->\n"
                        + "    guru.nidi.codeassert.dependency.a.A1\n"
                        + "    guru.nidi.codeassert.dependency.b.B1\n",
                analyze(Scope.CLASSES, collector), hasNoCycles());
    }

    private void assertMatcher(String message, DependencyResult result, Matcher<DependencyResult> matcher) {
        assertFalse(matcher.matches(result), "Should not match");
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertEquals(message, sd.toString());
    }

    private static String base(String s) {
        return BASE + s;
    }
}
