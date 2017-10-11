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

import java.util.EnumSet;

public class CodeAssertTestBase {
    private DependencyResult dependencyResult;
    private FindBugsResult findBugsResult;
    private PmdResult pmdResult;
    private CpdResult cpdResult;
    private CheckstyleResult checkstyleResult;

    protected EnumSet<CodeAssertTestType> defaultTests() {
        return EnumSet.allOf(CodeAssertTestType.class);
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
}
