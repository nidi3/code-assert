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

import org.junit.Ignore;
import org.junit.Test;

import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
import static guru.nidi.codeassert.junit.CodeAssertTestType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@Ignore("This is made to be subclassed")
public class CodeAssertTest extends CodeAssertTestBase {
    @Test
    public void dependencies() {
        assumeTrue("Dependencies test excluded.", defaultTests().contains(DEPENDENCIES));
        assumeFalse("analyzeDependencies() not implemented.", dependencyResult() == null);
        assertThat(dependencyResult(), matchesRulesExactly());
    }

    @Test
    public void circularDependencies() {
        assumeTrue("Circular dependencies test excluded.", defaultTests().contains(CIRCULAR_DEPENDENCIES));
        assumeFalse("analyzeDependencies() not implemented.", dependencyResult() == null);
        assertThat(dependencyResult(), hasNoCycles());
    }

    @Test
    public void findBugs() {
        assumeTrue("FindBugs test excluded.", defaultTests().contains(FIND_BUGS));
        assumeFalse("analyzeFindBugs() not implemented.", findBugsResult() == null);
        assertThat(findBugsResult(), hasNoBugs());
    }

    @Test
    public void findBugsUnusedActions() {
        assumeTrue("FindBugs - unused actions test excluded.", defaultTests().contains(FIND_BUGS_UNUSED_ACTIONS));
        assumeFalse("analyzeFindBugs() not implemented.", findBugsResult() == null);
        assertThat(findBugsResult(), hasNoUnusedActions());
    }

    @Test
    public void pmdViolations() {
        assumeTrue("PMD test excluded.", defaultTests().contains(PMD));
        assumeFalse("analyzePmd() not implemented.", pmdResult() == null);
        assertThat(pmdResult(), hasNoPmdViolations());
    }

    @Test
    public void pmdUnusedActions() {
        assumeTrue("PMD - unused actions test excluded.", defaultTests().contains(PMD_UNUSED_ACTIONS));
        assumeFalse("analyzePmd() not implemented.", pmdResult() == null);
        assertThat(pmdResult(), hasNoUnusedActions());
    }

    @Test
    public void cpd() {
        assumeTrue("CPD test excluded.", defaultTests().contains(CPD));
        assumeFalse("analyzeCpd() not implemented.", cpdResult() == null);
        assertThat(cpdResult(), hasNoCodeDuplications());
    }

    @Test
    public void cpdUnusedActions() {
        assumeTrue("CPD - unused actions test excluded.", defaultTests().contains(CPD_UNUSED_ACTIONS));
        assumeFalse("analyzeCpd() not implemented.", cpdResult() == null);
        assertThat(cpdResult(), hasNoUnusedActions());
    }

    @Test
    public void checkstyle() {
        assumeTrue("Checkstyle test excluded.", defaultTests().contains(CHECKSTYLE));
        assumeFalse("analyzeCheckstyle() not implemented.", checkstyleResult() == null);
        assertThat(checkstyleResult(), hasNoCheckstyleIssues());
    }

    @Test
    public void checkstyleUnusedActions() {
        assumeTrue("Checkstyle - unused actions test excluded.", defaultTests().contains(CHECKSTYLE_UNUSED_ACTIONS));
        assumeFalse("analyzeCheckstyle() not implemented.", checkstyleResult() == null);
        assertThat(checkstyleResult(), hasNoUnusedActions());
    }

}
