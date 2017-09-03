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
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AnalyzerTest {
    final Model model = new ModelBuilder(
            AnalyzerConfig.maven().mainAndTest("guru/nidi/codeassert/model")).build();

    @Test
    public void packages() throws IOException {
        assertEquals(38, model.getPackages().size());
    }

    @Test
    public void classes() throws IOException {
        assertEquals(117, model.getClasses().size());
    }
}
