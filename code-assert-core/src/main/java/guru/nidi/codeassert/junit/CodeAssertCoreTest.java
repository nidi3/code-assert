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

import static guru.nidi.codeassert.junit.CodeAssertCoreMatchers.*;
import static guru.nidi.codeassert.junit.CodeAssertTestType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@Ignore("This is made to be subclassed")
public class CodeAssertCoreTest extends CodeAssertCoreTestBase {
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
    public void codeStructure() {
        assumeTrue("Structure tests excluded.", defaultTests().contains(STRUCTURE));
        assumeFalse("createModel() not implemented.", createModel() == null);
        assertThat(createModel(), exposesNoInternalTypes());
    }
}
