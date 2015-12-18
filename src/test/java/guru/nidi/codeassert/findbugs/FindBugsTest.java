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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.Priorities;
import guru.nidi.codeassert.AnalyzerConfig;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.findbugs.FindBugsMatchers.hasNoIssues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class FindBugsTest {
    private final AnalyzerConfig config =AnalyzerConfig.mavenMainAndTestClasses();
    private final BugCollector bugCollector = BugCollector.simple(null, Priorities.NORMAL_PRIORITY, "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD", "DLS_DEAD_LOCAL_STORE", "SIC_INNER_SHOULD_BE_STATIC", "UC_USELESS_OBJECT", "OBL_UNSATISFIED_OBLIGATION");

    @Test
    public void simple() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config,
                BugCollector.simple(17, Priorities.NORMAL_PRIORITY));
        assertEquals(25, analyzer.analyze().size());
    }

    @Test
    public void globalIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector);

        assertMatcher("\n" +
                        "15 H UC_USELESS_VOID_METHOD                   Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless  At ExampleConcreteClass.java:[line 51]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 23]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 27]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 32]\n" +
                        "18 M URF_UNREAD_FIELD                         Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2  At GenericParameters.java:[line 37]",
                analyzer, hasNoIssues());
    }

    @Test
    public void classNameIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config,
                bugCollector.andIgnore("Bugs", "DM_NUMBER_CTOR"));

        assertMatcher("\n" +
                        "15 H UC_USELESS_VOID_METHOD                   Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless  At ExampleConcreteClass.java:[line 51]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 32]\n" +
                        "18 M URF_UNREAD_FIELD                         Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2  At GenericParameters.java:[line 37]",
                analyzer, hasNoIssues());
    }

    @Test
    public void fullClassIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config,
                bugCollector.andIgnore("guru.nidi.codeassert.Bugs", "DM_NUMBER_CTOR"));

        assertMatcher("\n" +
                        "15 H UC_USELESS_VOID_METHOD                   Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless  At ExampleConcreteClass.java:[line 51]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 32]\n" +
                        "18 M URF_UNREAD_FIELD                         Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2  At GenericParameters.java:[line 37]",
                analyzer, hasNoIssues());
    }

    @Test
    public void fullClassMethodIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config,
                bugCollector.andIgnore("guru.nidi.codeassert.Bugs#bugs", "DM_NUMBER_CTOR"));

        assertMatcher("\n" +
                        "15 H UC_USELESS_VOID_METHOD                   Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless  At ExampleConcreteClass.java:[line 51]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 27]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 32]\n" +
                        "18 M URF_UNREAD_FIELD                         Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2  At GenericParameters.java:[line 37]",
                analyzer, hasNoIssues());
    }

    @Test
    public void methodIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config,
                bugCollector.andIgnore("#bugs", "DM_NUMBER_CTOR"));

        assertMatcher("\n" +
                        "15 H UC_USELESS_VOID_METHOD                   Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless  At ExampleConcreteClass.java:[line 51]\n" +
                        "18 M DM_NUMBER_CTOR                           guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead  At Bugs.java:[line 27]\n" +
                        "18 M URF_UNREAD_FIELD                         Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2  At GenericParameters.java:[line 37]",
                analyzer, hasNoIssues());
    }

    private void assertMatcher(String message, FindBugsAnalyzer analyzer, Matcher<FindBugsAnalyzer> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }
}
