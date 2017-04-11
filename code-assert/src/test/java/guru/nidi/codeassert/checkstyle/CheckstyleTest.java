/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.checkstyle;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.junit.CodeAssertMatchers;
import org.junit.Test;

import static guru.nidi.codeassert.config.CollectorConfig.just;
import static org.junit.Assert.assertThat;

public class CheckstyleTest {
    private final AnalyzerConfig config = AnalyzerConfig.maven().mainAndTest();

    @Test
    public void simple() {
        final CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer(config, new StyleEventCollector().severity(SeverityLevel.WARNING).config(
                just(In.everywhere().ignore("empty.line.separator")),
                just(In.clazz(CheckstyleAnalyzer.class).ignore("import.avoidStar"))

        ));
        assertThat(analyzer.analyze(), CodeAssertMatchers.hasNoCheckstyleIssues());
    }

}
