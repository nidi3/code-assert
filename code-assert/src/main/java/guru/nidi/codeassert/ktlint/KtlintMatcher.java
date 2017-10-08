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
package guru.nidi.codeassert.ktlint;

import guru.nidi.codeassert.util.ResultMatcher;
import org.hamcrest.Description;

public class KtlintMatcher extends ResultMatcher<KtlintResult, LocatedLintError> {
    public void describeTo(Description description) {
        description.appendText("Has no ktlint issues");
    }

    @Override
    protected void describeMismatchSafely(KtlintResult item, Description description) {
        for (final LocatedLintError error : item.findings()) {
            description.appendText("\n").appendText(printError(error));
        }
    }

    private String printError(LocatedLintError error) {
        return String.format("%-35s %s:%d    %s",
                error.ruleId, error.file.getAbsolutePath(), error.line, error.detail);
    }
}
