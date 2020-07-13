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
package guru.nidi.codeassert;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.List;

public class AnalyzerResult<T> {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final Analyzer<T> analyzer;
    private final T findings;
    private final List<String> unusedActions;

    public AnalyzerResult(Analyzer<T> analyzer, T findings, List<String> unusedActions) {
        this.analyzer = analyzer;
        this.findings = findings;
        this.unusedActions = unusedActions;
    }

    public Analyzer<T> analyzer() {
        return analyzer;
    }

    public T findings() {
        return findings;
    }

    public List<String> unusedActions() {
        return unusedActions;
    }
}
