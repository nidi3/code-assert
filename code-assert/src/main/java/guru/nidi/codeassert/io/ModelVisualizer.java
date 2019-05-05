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

import guru.nidi.codeassert.model.CodePackage;
import guru.nidi.codeassert.model.Model;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;

import java.util.function.Function;

import static guru.nidi.graphviz.model.Factory.mutGraph;

public class ModelVisualizer {
    private final Model model;

    public ModelVisualizer(Model model) {
        this.model = model;
    }

    public Visualized visualizePackages(Function<CodePackage, MutableNode> transform) {
        final MutableGraph graph = CreationContext.use(ctx -> {
            final MutableGraph g = mutGraph().setDirected(true)
                    .graphAttrs().add(RankDir.LEFT_TO_RIGHT);
            for (CodePackage pack : model.getPackages()) {
                g.add(transform.apply(pack));
            }
            /*
            for (CodeClass clazz : model.getClasses()) {
                final MutableNode source = mutNode(clazz.getSimpleName()).add(Shape.ELLIPSE);
                if (model.isOwnPackage(clazz.getPackage())) {
                    final Double cover = coverage.getOrDefault(clazz.getName(), 1D);
                    source.add(Shape.RECTANGLE)
                            .add(Size.mode(FIXED).size(1 + clazz.getTotalSize() / 5000.0, 1))
                            .add(Color.rgb(255 - (int) (2.55 * cover), (int) (2.55 * cover) - 255, 0).fill())
                            .add(Style.FILLED);
                }
                g.add(source);
                for (CodeClass dep : clazz.uses()) {
                    source.addLink(dep.getSimpleName());
                }
            }
            */
            return g;
        });
        return new Visualized(Graphviz.fromGraph(graph));
    }

    public static Function<String, String> replaceFunc(String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of replacement parameters expected.");
        }
        return name -> {
            for (int i = 0; i < replacements.length; i += 2) {
                if (name.startsWith(replacements[i])) {
                    return replacements[i + 1] + name.substring(replacements[i].length());
                }
            }
            return name;
        };
    }
}
