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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static guru.nidi.codeassert.junit.CodeAssertCoreMatchers.*;
import static guru.nidi.codeassert.junit.CodeAssertMatchers.*;
import static guru.nidi.codeassert.junit.CodeAssertTestType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Disabled("This is made to be subclassed")
public class CodeAssertJunit5Test extends CodeAssertTestBase {
    @Test
    void dependencies() {
        assumeTrue(defaultTests().contains(DEPENDENCIES), "Dependencies test excluded.");
        assumeFalse(dependencyResult() == null, "analyzeDependencies() not implemented.");
        assertThat(dependencyResult(), matchesRulesExactly());
    }

    @Test
    void circularDependencies() {
        assumeTrue(defaultTests().contains(CIRCULAR_DEPENDENCIES), "Circular dependencies test excluded.");
        assumeFalse(dependencyResult() == null, "analyzeDependencies() not implemented.");
        assertThat(dependencyResult(), hasNoCycles());
    }

    @Test
    void codeStructure() {
        assumeTrue(defaultTests().contains(STRUCTURE), "Structure tests excluded.");
        assumeFalse(createModel() == null, "createModel() not implemented.");
        assertThat(createModel(), exposesNoInternalTypes());
    }

    @Test
    void findBugs() {
        assumeTrue(defaultTests().contains(FIND_BUGS), "FindBugs test excluded.");
        assumeFalse(findBugsResult() == null, "analyzeFindBugs() not implemented.");
        assertThat(findBugsResult(), hasNoBugs());
    }

    @Test
    void findBugsUnusedActions() {
        assumeTrue(defaultTests().contains(FIND_BUGS_UNUSED_ACTIONS), "FindBugs - unused actions test excluded.");
        assumeFalse(findBugsResult() == null, "analyzeFindBugs() not implemented.");
        assertThat(findBugsResult(), hasNoUnusedActions());
    }

    @Test
    void pmdViolations() {
        assumeTrue(defaultTests().contains(PMD), "PMD test excluded.");
        assumeFalse(pmdResult() == null, "analyzePmd() not implemented.");
        assertThat(pmdResult(), hasNoPmdViolations());
    }

    @Test
    void pmdUnusedActions() {
        assumeTrue(defaultTests().contains(PMD_UNUSED_ACTIONS), "PMD - unused actions test excluded.");
        assumeFalse(pmdResult() == null, "analyzePmd() not implemented.");
        assertThat(pmdResult(), hasNoUnusedActions());
    }

    @Test
    void cpd() {
        assumeTrue(defaultTests().contains(CPD), "CPD test excluded.");
        assumeFalse(cpdResult() == null, "analyzeCpd() not implemented.");
        assertThat(cpdResult(), hasNoCodeDuplications());
    }

    @Test
    void cpdUnusedActions() {
        assumeTrue(defaultTests().contains(CPD_UNUSED_ACTIONS), "CPD - unused actions test excluded.");
        assumeFalse(cpdResult() == null, "analyzeCpd() not implemented.");
        assertThat(cpdResult(), hasNoUnusedActions());
    }

    @Test
    void checkstyle() {
        assumeTrue(defaultTests().contains(CHECKSTYLE), "Checkstyle test excluded.");
        assumeFalse(checkstyleResult() == null, "analyzeCheckstyle() not implemented.");
        assertThat(checkstyleResult(), hasNoCheckstyleIssues());
    }

    @Test
    void checkstyleUnusedActions() {
        assumeTrue(defaultTests().contains(CHECKSTYLE_UNUSED_ACTIONS), "Checkstyle - unused actions test excluded.");
        assumeFalse(checkstyleResult() == null, "analyzeCheckstyle() not implemented.");
        assertThat(checkstyleResult(), hasNoUnusedActions());
    }

}
