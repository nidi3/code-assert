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

import guru.nidi.codeassert.dependency.DependencyResult;
import guru.nidi.codeassert.model.Model;

import java.util.EnumSet;

public class CodeAssertCoreTestBase {
    private Model model;
    private DependencyResult dependencyResult;

    protected EnumSet<CodeAssertTestType> defaultTests() {
        return EnumSet.allOf(CodeAssertTestType.class);
    }

    protected Model createModel() {
        return null;
    }

    protected DependencyResult analyzeDependencies() {
        return null;
    }

    protected synchronized Model model() {
        if (model == null) {
            model = createModel();
        }
        return model;
    }

    protected synchronized DependencyResult dependencyResult() {
        if (dependencyResult == null) {
            dependencyResult = analyzeDependencies();
        }
        return dependencyResult;
    }
}
