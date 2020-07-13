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
import static guru.nidi.codeassert.junit.CodeAssertTestType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Disabled("This is made to be subclassed")
public class CodeAssertCoreJunit5Test extends CodeAssertCoreTestBase {
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
}
