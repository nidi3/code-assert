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

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Visualized {
    private final Graphviz graphviz;

    Visualized(Graphviz graphviz) {
        this.graphviz = graphviz;
    }

    public void toFile(File target) throws IOException {
        graphviz.render(Format.PNG).toFile(target);
    }

    public BufferedImage asImage() {
        return graphviz.render(Format.PNG).toImage();
    }
}
