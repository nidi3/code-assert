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

import guru.nidi.codeassert.AnalyzerConfig;
import guru.nidi.codeassert.model.ModelAnalyzer;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Test;

import static guru.nidi.codeassert.PackageCollector.all;
import static guru.nidi.codeassert.dependency.CycleResult.packages;
import static guru.nidi.codeassert.dependency.DependencyMatchers.hasNoCycles;
import static guru.nidi.codeassert.dependency.DependencyMatchers.hasNoCyclesExcept;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class CycleTest {
    private static final String BASE = "guru.nidi.codeassert.dependency.";
    private ModelAnalyzer project;

    @Before
    public void analyze() {
        project = new ModelAnalyzer(
                AnalyzerConfig.mavenTestClasses("guru/nidi/codeassert/dependency")
                        .collecting(all().excluding("java.", "org")));
        project.analyze();
    }

    @Test
    public void cycles() {
        final Matcher<ModelAnalyzer> matcher = hasNoCycles();
        assertMatcher("Found these cyclic groups:\n" +
                        "\n" +
                        "- Group of 3: guru.nidi.codeassert.dependency.a.a, guru.nidi.codeassert.dependency.b.a, guru.nidi.codeassert.dependency.c.a\n" +
                        "  guru.nidi.codeassert.dependency.a.a ->\n" +
                        "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.a.a.Aa1)\n" +
                        "  guru.nidi.codeassert.dependency.b.a ->\n" +
                        "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n" +
                        "    guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n" +
                        "  guru.nidi.codeassert.dependency.c.a ->\n" +
                        "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n" +
                        "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n" +
                        "\n" +
                        "- Group of 3: guru.nidi.codeassert.dependency.a, guru.nidi.codeassert.dependency.b, guru.nidi.codeassert.dependency.c\n" +
                        "  guru.nidi.codeassert.dependency.a ->\n" +
                        "    guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.a.A1)\n" +
                        "  guru.nidi.codeassert.dependency.b ->\n" +
                        "    guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.b.B1)\n" +
                        "    guru.nidi.codeassert.dependency.c (by guru.nidi.codeassert.dependency.b.B1)\n" +
                        "  guru.nidi.codeassert.dependency.c ->\n" +
                        "    guru.nidi.codeassert.dependency.a (by guru.nidi.codeassert.dependency.c.C1)\n" +
                        "    guru.nidi.codeassert.dependency.b (by guru.nidi.codeassert.dependency.c.C1, guru.nidi.codeassert.dependency.c.C2)\n",
                matcher);
    }

    @Test
    public void cyclesWithExceptions() {
        final Matcher<ModelAnalyzer> matcher = hasNoCyclesExcept(
                packages(base("a"), base("b"), base("c")),
                packages(base("a.a")),
                packages(base("b.a"), base("c.a")));
        assertMatcher("Found these cyclic groups:\n" +
                        "\n" +
                        "- Group of 3: guru.nidi.codeassert.dependency.a.a, guru.nidi.codeassert.dependency.b.a, guru.nidi.codeassert.dependency.c.a\n" +
                        "  guru.nidi.codeassert.dependency.a.a ->\n" +
                        "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.a.a.Aa1)\n" +
                        "  guru.nidi.codeassert.dependency.b.a ->\n" +
                        "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.b.a.Ba1)\n" +
                        "    guru.nidi.codeassert.dependency.c.a (by guru.nidi.codeassert.dependency.b.a.Ba2)\n" +
                        "  guru.nidi.codeassert.dependency.c.a ->\n" +
                        "    guru.nidi.codeassert.dependency.a.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n" +
                        "    guru.nidi.codeassert.dependency.b.a (by guru.nidi.codeassert.dependency.c.a.Ca1)\n",
                matcher);
    }

    private void assertMatcher(String message, Matcher<ModelAnalyzer> matcher) {
        assertFalse("Should not match", matcher.matches(project));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(project, sd);
        assertEquals(message, sd.toString());
    }

    private static String base(String s) {
        return BASE + s;
    }
}
