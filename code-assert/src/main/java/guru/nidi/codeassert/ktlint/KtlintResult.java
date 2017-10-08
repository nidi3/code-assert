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

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerResult;

import java.util.List;

public class KtlintResult extends AnalyzerResult<List<LocatedLintError>> {
    public KtlintResult(Analyzer<List<LocatedLintError>> analyzer,
                        List<LocatedLintError> findings, List<String> unusedActions) {
        super(analyzer, findings, unusedActions);
    }
}
