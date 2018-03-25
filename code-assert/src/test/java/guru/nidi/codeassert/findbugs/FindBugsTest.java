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
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.jacoco.Coverage;
import guru.nidi.codeassert.pmd.PmdRulesets;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.io.File;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoBugs;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.hasNoUnusedActions;
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
                            "OBL_UNSATISFIED_OBLIGATION", "EI_EXPOSE_REP", "EI_EXPOSE_REP2"),
                    In.classes("*Comparator").ignore("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE"))
            .because("avoid jvm killed on travis",
                    In.classes("*Test").ignore("DM_GC"))
            .because("it's ok",
                    In.clazz(Coverage.class).ignore("EQ_COMPARETO_USE_OBJECT_EQUALS"),
                    In.clazz(AnalyzerConfigTest.class).ignore("DMI_HARDCODED_ABSOLUTE_FILENAME"),
                    In.everywhere().ignore("PATH_TRAVERSAL_IN"))
            .because("is handled by annotation",
                    In.clazz(PmdRulesets.class).ignore("URF_UNREAD_FIELD"));

    @Test
    void simple() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, new BugCollector().maxRank(17).minPriority(Priorities.NORMAL_PRIORITY));
        assertThat(analyzer.analyze().findings().size(), equalTo(46));
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
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 20, "guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 33, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void classNameIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.classes("Bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void classIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void innerClassIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.InnerBugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 20, "guru.nidi.codeassert.Bugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void fullClassIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.clazz(Bugs.class).ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void fullClassMethodIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.loc("guru.nidi.codeassert.Bugs#bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 33, "guru.nidi.codeassert.Bugs$InnerBugs.bugs() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void methodIgnore() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.methods("bugs").ignore("DM_NUMBER_CTOR")));
        assertMatcher(""
                        + line(15, "H", "UC_USELESS_VOID_METHOD", "model/ExampleConcreteClass", 52, "Method guru.nidi.codeassert.model.ExampleConcreteClass.c(BigDecimal, byte[]) seems to be useless")
                        + line(18, "M", "DM_NUMBER_CTOR", "Bugs", 24, "guru.nidi.codeassert.Bugs.more() invokes inefficient new Integer(int) constructor; use Integer.valueOf(int) instead")
                        + line(18, "M", "URF_UNREAD_FIELD", "model/p4/GenericParameters", 34, "Unread field: guru.nidi.codeassert.model.p4.GenericParameters.l2"),
                analyzer.analyze(), hasNoBugs());
    }

    @Test
    void unusedActions() {
        System.gc();
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(config, bugCollector.just(In.methods("bugs").ignore("BLA")));
        assertMatcher("Found unused actions:\n    ignore [BLA] in [#bugs]", analyzer.analyze(), hasNoUnusedActions());
    }

    private <T extends AnalyzerResult<?>> void assertMatcher(String message, T result, Matcher<T> matcher) {
        assertFalse(matcher.matches(result));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(result, sd);
        assertEquals(message, sd.toString());
    }

    private String line(int rank, String priority, String type, String relative, int line, String msg) {
        return String.format("%n%-2d %-8s %-45s %s:%d    %s", rank, priority, type, new File("src/test/java/guru/nidi/codeassert/" + relative + ".java").getAbsolutePath(), line, msg);
    }
}
