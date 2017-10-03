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
package guru.nidi.codeassert.junit;

import guru.nidi.codeassert.checkstyle.CheckstyleResult;
import guru.nidi.codeassert.dependency.DependencyResult;
import guru.nidi.codeassert.findbugs.FindBugsResult;
import guru.nidi.codeassert.pmd.CpdResult;
import guru.nidi.codeassert.pmd.PmdResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
import static guru.nidi.codeassert.junit.CodeAssertTest.Type.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Disabled("This is made to be subclassed")
public class CodeAssertTest {
    public enum Type {
        CIRCULAR_DEPENDENCIES,
        FIND_BUGS, FIND_BUGS_UNUSED_ACTIONS,
        PMD, PMD_UNUSED_ACTIONS,
        CPD, CPD_UNUSED_ACTIONS,
        CHECKSTYLE, CHECKSTYLE_UNUSED_ACTIONS
    }

    private static DependencyResult dependencyResult;
    private static FindBugsResult findBugsResult;
    private static PmdResult pmdResult;
    private static CpdResult cpdResult;
    private static CheckstyleResult checkstyleResult;

    protected EnumSet<Type> defaultTests() {
        return EnumSet.allOf(Type.class);
    }

    protected DependencyResult analyzeDependencies() {
        return null;
    }

    protected FindBugsResult analyzeFindBugs() {
        return null;
    }

    protected PmdResult analyzePmd() {
        return null;
    }

    protected CpdResult analyzeCpd() {
        return null;
    }

    protected CheckstyleResult analyzeCheckstyle() {
        return null;
    }

    protected synchronized DependencyResult dependencyResult() {
        if (dependencyResult == null) {
            dependencyResult = analyzeDependencies();
        }
        return dependencyResult;
    }

    protected synchronized FindBugsResult findBugsResult() {
        if (findBugsResult == null) {
            findBugsResult = analyzeFindBugs();
        }
        return findBugsResult;
    }

    protected synchronized PmdResult pmdResult() {
        if (pmdResult == null) {
            pmdResult = analyzePmd();
        }
        return pmdResult;
    }

    protected synchronized CpdResult cpdResult() {
        if (cpdResult == null) {
            cpdResult = analyzeCpd();
        }
        return cpdResult;
    }

    protected synchronized CheckstyleResult checkstyleResult() {
        if (checkstyleResult == null) {
            checkstyleResult = analyzeCheckstyle();
        }
        return checkstyleResult;
    }

    @Test
    public void circularDependencies() {
        assumeFalse(dependencyResult() == null, "analyzeDependencies() not implemented.");
        assumeTrue(defaultTests().contains(CIRCULAR_DEPENDENCIES), "Circular dependencies test excluded.");
        assertThat(dependencyResult(), hasNoCycles());
    }

    @Test
    public void findBugs() {
        assumeFalse(findBugsResult() == null, "analyzeFindBugs() not implemented.");
        assumeTrue(defaultTests().contains(FIND_BUGS), "FindBugs test excluded.");
        assertThat(findBugsResult(), hasNoBugs());
    }

    @Test
    public void findBugsUnusedActions() {
        assumeFalse(findBugsResult() == null, "analyzeFindBugs() not implemented.");
        assumeTrue(defaultTests().contains(FIND_BUGS_UNUSED_ACTIONS), "FindBugs - unused actions test excluded.");
        assertThat(findBugsResult(), hasNoUnusedActions());
    }

    @Test
    public void pmdViolations() {
        assumeFalse(pmdResult() == null, "analyzePmd() not implemented.");
        assumeTrue(defaultTests().contains(PMD), "PMD test excluded.");
        assertThat(pmdResult(), hasNoPmdViolations());
    }

    @Test
    public void pmdUnusedActions() {
        assumeFalse(pmdResult() == null, "analyzePmd() not implemented.");
        assumeTrue(defaultTests().contains(PMD_UNUSED_ACTIONS), "PMD - unused actions test excluded.");
        assertThat(pmdResult(), hasNoUnusedActions());
    }

    @Test
    public void cpd() {
        assumeFalse(cpdResult() == null, "analyzeCpd() not implemented.");
        assumeTrue(defaultTests().contains(CPD), "CPD test excluded.");
        assertThat(cpdResult(), hasNoCodeDuplications());
    }

    @Test
    public void cpdUnusedActions() {
        assumeFalse(cpdResult() == null, "analyzeCpd() not implemented.");
        assumeTrue(defaultTests().contains(CPD_UNUSED_ACTIONS), "CPD - unused actions test excluded.");
        assertThat(cpdResult(), hasNoUnusedActions());
    }

    @Test
    public void checkstyle() {
        assumeFalse(checkstyleResult() == null, "analyzeCheckstyle() not implemented.");
        assumeTrue(defaultTests().contains(CHECKSTYLE), "Checkstyle test excluded.");
        assertThat(checkstyleResult(), hasNoCheckstyleIssues());
    }

    @Test
    public void checkstyleUnusedActions() {
        assumeFalse(checkstyleResult() == null, "analyzeCheckstyle() not implemented.");
        assumeTrue(defaultTests().contains(CHECKSTYLE_UNUSED_ACTIONS), "Checkstyle - unused actions test excluded.");
        assertThat(checkstyleResult(), hasNoUnusedActions());
    }

}
