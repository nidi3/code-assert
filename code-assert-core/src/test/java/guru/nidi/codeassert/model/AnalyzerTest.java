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
package guru.nidi.codeassert.model;

import guru.nidi.codeassert.config.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyzerTest {
    final Model model = Model.from(AnalyzerConfig.maven().mainAndTest("guru/nidi/codeassert/model").getClasses()).read();

    @Test
    void packages() {
        assertEquals(41, model.getPackages().size());
    }

    @Test
    void classes() {
        assertThat(model.getClasses().size(), anyOf(equalTo(139), equalTo(140), equalTo(141), equalTo(142)));
    }
}
