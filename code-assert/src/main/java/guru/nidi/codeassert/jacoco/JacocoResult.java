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
package guru.nidi.codeassert.jacoco;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerResult;
import guru.nidi.codeassert.config.ValuedLocation;

import java.util.List;

public class JacocoResult extends AnalyzerResult<List<ValuedLocation>> {
    private final CoverageType[] types;

    public JacocoResult(Analyzer<List<ValuedLocation>> analyzer, List<ValuedLocation> findings,
                        List<String> unusedActions, CoverageType[] types) {
        super(analyzer, findings, unusedActions);
        this.types = types;
    }

    public CoverageType[] getTypes() {
        return types;
    }
}
