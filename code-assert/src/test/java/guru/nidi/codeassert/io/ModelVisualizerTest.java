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
import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.jacoco.*;
import guru.nidi.codeassert.model.*;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.model.MutableNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static guru.nidi.codeassert.config.CollectorConfig.just;
import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;
import static guru.nidi.codeassert.io.ModelVisualizer.replaceFunc;
import static guru.nidi.graphviz.attribute.Size.Mode.FIXED;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Factory.to;
import static java.util.stream.Collectors.toMap;
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

        class GuruNidiCodeassert extends DependencyRuler {
            DependencyRule checkstyleLib = denyRule("com.puppycrawl.tools.checkstyle").andAllSub();
            DependencyRule detektLib = denyRule("io.gitlab.arturbosch.detekt").andAllSub();
            DependencyRule findBugsLib = denyRule("edu.umd.cs.findbugs").andAllSub();
            DependencyRule ktlintLib = denyRule("com.github.shyiko.ktlint").andAllSub();
            DependencyRule pmdLib = denyRule("net.sourceforge.pmd").andAllSub();
            DependencyRule graphvizLib = denyRule("guru.nidi.graphviz").andAllSub();
            DependencyRule config, dependency, findbugs, checkstyle, detekt, io, model, pmd, ktlint, util, junit, junitKotlin, jacoco;

            @Override
            public void defineRules() {
                base().mayBeUsedBy(all());
                config.mayBeUsedBy(all());
                util.mayBeUsedBy(all());
                dependency.mayUse(model);
                junit.mayUse(model, dependency, findbugs, checkstyle, pmd);
                junitKotlin.mayUse(ktlint, detekt);
                checkstyle.mayUse(checkstyleLib);
                detekt.mayUse(detektLib);
                findbugs.mayUse(findBugsLib);
                io.mayUse(jacoco, model, graphvizLib);
                ktlint.mayUse(ktlintLib);
                pmd.mayUse(pmdLib).mustUse(io);
            }
        }

        final DependencyRules rules = denyAll()
                .withExternals("java.*", "org.*", "kotlin.*")
                .withRelativeRules(new GuruNidiCodeassert());
        final Dependencies dependencies = new DependencyAnalyzer(model).rules(rules).analyze().findings();

        final Map<String, Double> coverage = new JacocoAnalyzer(new CoverageCollector(CoverageType.LINE)
                .config(just(For.allPackages().setMinima(100))))
                .analyze()
                .findings().stream()
                .collect(toMap(f -> f.getPack(), f -> f.getValues()[0]));

        final Function<String, String> labelFunc = replaceFunc("guru.nidi.codeassert", "");
        final ModelVisualizer visualizer = new ModelVisualizer(model);
        final File target = new File("images/packages.png");
        visualizer.visualizePackages(pack -> {
            final MutableNode node = mutNode(labelFunc.apply(pack.getName())).add(Shape.ELLIPSE);
            if (model.isOwnPackage(pack)) {
                final Double cover = coverage.getOrDefault(pack.getName(), 1D);
                final int codeSize = pack.getClasses().stream().mapToInt(CodeClass::getTotalSize).sum();
                node.add(Shape.RECTANGLE)
                        .add(Size.mode(FIXED).size(1 + codeSize / 20000.0, .5 + pack.getClasses().size() / 10.0))
                        .add(Color.rgb(255 - (int) (2.55 * cover), (int) (2.55 * cover) - 255, 0).fill())
                        .add(Style.FILLED);
            }
            for (String missing : dependencies.getMissing(pack.getName(), pack.uses(), CodePackage::getName)) {
                node.addLink(to(mutNode(labelFunc.apply(missing))).with(Style.DASHED, Color.RED));
            }
            for (CodePackage dep : pack.uses()) {
                final boolean denied = dependencies.isDenied(pack.getName(), dep.getName());
                node.addLink(to(mutNode(labelFunc.apply(dep.getName()))).with(denied ? Color.RED : Color.BLACK));
            }
            return node;
        }).toFile(target);
        assertTrue(target.exists());
    }
}
