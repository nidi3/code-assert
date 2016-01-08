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
import guru.nidi.codeassert.Bugs;
import guru.nidi.codeassert.config.Analyzer;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static guru.nidi.codeassert.findbugs.FindBugsMatchers.findsNoBugs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class FindBugsTest {
    private final AnalyzerConfig config = AnalyzerConfig.mavenMainAndTestClasses();
    private final BugCollector bugCollector = new BugCollector().minPriority(Priorities.NORMAL_PRIORITY)
            .because("is not useful",
                    In.everywhere().ignore("UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD", "DLS_DEAD_LOCAL_STORE", "SIC_INNER_SHOULD_BE_STATIC", "UC_USELESS_OBJECT", "OBL_UNSATISFIED_OBLIGATION"),
                    In.loc("*Comparator").ignore("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"))
            .because("is handled by annotation",
                    In.loc("Rulesets$*").ignore("URF_UNREAD_FIELD"));

    @Test
    public void simple() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, new BugCollector().maxRank(17).minPriority(Priorities.NORMAL_PRIORITY));
        assertEquals(16, analyzer.analyze().size());
    }

    @Test
    public void globalIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector);
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 23, "guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 27, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 36, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    @Test
    public void classNameIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.loc("Bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 36, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    @Test
    public void classIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 36, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    @Test
    public void innerClassIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.InnerBugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 23, "guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 27, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    @Test
    public void fullClassIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.loc("guru.nidi.codeassert.Bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 36, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    @Test
    public void fullClassMethodIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.loc("guru.nidi.codeassert.Bugs#bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 27, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 36, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    @Test
    public void methodIgnore() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.loc("#bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher("" +
                        line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless") +
                        line(18, "M", "DM_NUMBER_CTOR", "Bugs", 27, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead") +
                        line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 37, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer, findsNoBugs());
    }

    private <T extends Analyzer<?>> void assertMatcher(String message, T analyzer, Matcher<T> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }

    private String line(int rank, String priority, String type, String relative, int line, String msg) {
        return String.format("%n%-2d %-8s %-45s %s:%d    %s", rank, priority, type, new File("src/test/java/guru/nidi/codeassert/" + relative + ".java").getAbsolutePath(), line, msg);
    }
}
