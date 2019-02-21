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
package guru.nidi.codeassert.io;

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.model.Model;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static guru.nidi.codeassert.io.ModelVisualizer.replaceFunc;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelVisualizerTest {
    @Test
    @Disabled("No coverage data available on CI server")
    void myself() throws IOException {
        final Model model = Model
                .from(AnalyzerConfig.maven().main().getClasses())
                .ignoringPackages("java", "org.hamcrest", "org.slf4j", "org.apache")
                .mergingPackages("edu.umd.cs.findbugs", "net.sourceforge.pmd", "io.gitlab.arturbosch.detekt", "com.puppycrawl.tools.checkstyle", "com.github.shyiko.ktlint", "kotlin", "org.junit", "guru.nidi.graphviz")
                .read();
        final ModelVisualizer visualizer = new ModelVisualizer(model).labelFunc(replaceFunc("guru.nidi.codeassert", ""));
        final File target = new File("images/packages.png");
        visualizer.visualize().toFile(target);
        assertTrue(target.exists());
    }
}
