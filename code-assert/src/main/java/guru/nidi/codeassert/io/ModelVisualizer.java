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

import guru.nidi.codeassert.config.For;
import guru.nidi.codeassert.jacoco.*;
import guru.nidi.codeassert.model.*;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;

import java.util.Map;
import java.util.function.Function;

import static guru.nidi.codeassert.config.CollectorConfig.just;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static java.util.stream.Collectors.toMap;

public class ModelVisualizer {
    private final Model model;
    private final Function<CodePackage, String> labelFunc;

    public ModelVisualizer(Model model) {
        this(model, CodePackage::getName);
    }

    public ModelVisualizer(Model model, Function<CodePackage, String> labelFunc) {
        this.model = model;
        this.labelFunc = labelFunc;
    }

    public ModelVisualizer labelFunc(Function<CodePackage, String> labelFunc) {
        return new ModelVisualizer(model, labelFunc);
    }

    public Visualized visualize() {
        final Map<String, Double> coverage = new JacocoAnalyzer(new CoverageCollector(CoverageType.LINE)
                .config(just(For.allPackages().setMinima(100))))
                .analyze()
                .findings().stream()
                .collect(toMap(f -> f.getPack(), f -> f.getValues()[0]));
        final MutableGraph graph = CreationContext.use(ctx -> {
            final MutableGraph g = mutGraph().setDirected(true)
                    .graphAttrs().add(RankDir.LEFT_TO_RIGHT);
            for (CodePackage pack : model.getPackages()) {
                final MutableNode source = mutNode(labelFunc.apply(pack)).add(Shape.ELLIPSE);
                if (model.isOwnPackage(pack)) {
                    final Double cover = coverage.getOrDefault(pack.getName(), 1D);
                    final int codeSize = pack.getClasses().stream().mapToInt(CodeClass::getTotalSize).sum();
                    source.add(Shape.RECTANGLE)
                            .add(Size.mode(Size.Mode.FIXED))
                            .add("height", .5 + pack.getClasses().size() / 10.0)
                            .add("width", 1 + codeSize / 20000.0)
                            .add(Color.rgb(255 - (int) (2.55 * cover), (int) (2.55 * cover) - 255, 0).fill())
                            .add(Style.FILLED);
                }
                g.add(source);
                for (CodePackage dep : pack.uses()) {
                    source.addLink(labelFunc.apply(dep));
                }
            }
            return g;
        });
        return new Visualized(Graphviz.fromGraph(graph));
    }

    public static Function<CodePackage, String> replaceFunc(String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of replacement parameters expected.");
        }
        return codePackage -> {
            final String name = codePackage.getName();
            for (int i = 0; i < replacements.length; i += 2) {
                if (name.startsWith(replacements[i])) {
                    return replacements[i + 1] + name.substring(replacements[i].length());
                }
            }
            return name;
        };
    }
}
