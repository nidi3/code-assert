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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.Priorities;
import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.Bugs;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.In;
import guru.nidi.codeassert.pmd.PmdRulesets;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.File;

import static guru.nidi.codeassert.junit.CodeAssertCoreMatchers.hasNoUnusedActions;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoBugs;
import static java.lang.System.lineSeparator;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class FindBugsTest {
    private final AnalyzerConfig config = AnalyzerConfig.maven().mainAndTest();
    private final BugCollector bugCollector = new BugCollector().minPriority(Priorities.NORMAL_PRIORITY)
            .because("is not useful",
                    In.everywhere().ignore(
                            "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD",
                            "DLS_DEAD_LOCAL_STORE", "SIC_INNER_SHOULD_BE_STATIC", "UC_USELESS_OBJECT",
                            "OBL_UNSATISFIED_OBLIGATION", "EI_EXPOSE_REP", "EI_EXPOSE_REP2"))
            .because("avoid jvm killed on travis",
                    In.classes("*Test").ignore("DM_GC"))
            .because("it's ok",
                    In.everywhere().ignore("PATH_TRAVERSAL_IN"))
            .because("is handled by annotation",
                    In.clazz(PmdRulesets.class).ignore("URF_UNREAD_FIELD"));

    @Test
    void simple() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, new BugCollector().maxRank(17).minPriority(Priorities.NORMAL_PRIORITY));
        assertThat(analyzer.analyze().findings().size(), equalTo(28));
    }

    @Test
    void noUnusedActions() {
        System.gc();
        assertThat(new FindBugsAnalyzer(config, new BugCollector().maxRank(17).minPriority(Priorities.NORMAL_PRIORITY)).analyze(), hasNoUnusedActions());
    }

    @Test
    void globalIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector);
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 20, "guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 33, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void classNameIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.classes("Bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void classIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void innerClassIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.InnerBugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 20, "guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void fullClassIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void fullClassMethodIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.loc("guru.nidi.codeassert.Bugs#bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 33, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void methodIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.methods("bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs2", 20, "new guru.nidi.codeassert.Bugs2() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void unusedActions() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.methods("bugs").ignore("BLA")));
        assertMatcher("Found unused actions:%n    ignore [BLA] in [#bugs]", analyzer.analyze(), hasNoUnusedActions());
    }

    private <T extends AnalyzerResult<?>> void assertMatcher(String message, T result, Matcher<T> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertEquals(message.replace("%n", lineSeparator()), sd.toString());
    }

    private String line(int rank, String priority, String type, String relative, int line, String msg) {
        return String.format("%n%-2d %-8s %-45s %s:%d    %s", rank, priority, type, new File("src/test/java/guru/nidi/codeassert/" + relative + ".java").getAbsolutePath(), line, msg);
    }
}
