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
        assumeFalse("analyzeDependencies() not implemented.", dependencyResult() == null);
        assumeTrue("Dependencies test excluded.", defaultTests().contains(DEPENDENCIES));
        assertThat(dependencyResult(), matchesRulesExactly());
    }

    @Test
    public void circularDependencies() {
        assumeFalse("analyzeDependencies() not implemented.", dependencyResult() == null);
        assumeTrue("Circular dependencies test excluded.", defaultTests().contains(CIRCULAR_DEPENDENCIES));
        assertThat(dependencyResult(), hasNoCycles());
    }

    @Test
    public void findBugs() {
        assumeFalse("analyzeFindBugs() not implemented.", findBugsResult() == null);
        assumeTrue("FindBugs test excluded.", defaultTests().contains(FIND_BUGS));
        assertThat(findBugsResult(), hasNoBugs());
    }

    @Test
    public void findBugsUnusedActions() {
        assumeFalse("analyzeFindBugs() not implemented.", findBugsResult() == null);
        assumeTrue("FindBugs - unused actions test excluded.", defaultTests().contains(FIND_BUGS_UNUSED_ACTIONS));
        assertThat(findBugsResult(), hasNoUnusedActions());
    }

    @Test
    public void pmdViolations() {
        assumeFalse("analyzePmd() not implemented.", pmdResult() == null);
        assumeTrue("PMD test excluded.", defaultTests().contains(PMD));
        assertThat(pmdResult(), hasNoPmdViolations());
    }

    @Test
    public void pmdUnusedActions() {
        assumeFalse("analyzePmd() not implemented.", pmdResult() == null);
        assumeTrue("PMD - unused actions test excluded.", defaultTests().contains(PMD_UNUSED_ACTIONS));
        assertThat(pmdResult(), hasNoUnusedActions());
    }

    @Test
    public void cpd() {
        assumeFalse("analyzeCpd() not implemented.", cpdResult() == null);
        assumeTrue("CPD test excluded.", defaultTests().contains(CPD));
        assertThat(cpdResult(), hasNoCodeDuplications());
    }

    @Test
    public void cpdUnusedActions() {
        assumeFalse("analyzeCpd() not implemented.", cpdResult() == null);
        assumeTrue("CPD - unused actions test excluded.", defaultTests().contains(CPD_UNUSED_ACTIONS));
        assertThat(cpdResult(), hasNoUnusedActions());
    }

    @Test
    public void checkstyle() {
        assumeFalse("analyzeCheckstyle() not implemented.", checkstyleResult() == null);
        assumeTrue("Checkstyle test excluded.", defaultTests().contains(CHECKSTYLE));
        assertThat(checkstyleResult(), hasNoCheckstyleIssues());
    }

    @Test
    public void checkstyleUnusedActions() {
        assumeFalse("analyzeCheckstyle() not implemented.", checkstyleResult() == null);
        assumeTrue("Checkstyle - unused actions test excluded.", defaultTests().contains(CHECKSTYLE_UNUSED_ACTIONS));
        assertThat(checkstyleResult(), hasNoUnusedActions());
    }

}
