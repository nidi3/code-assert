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

import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.AnalyzerConfig;

import java.io.File;
import java.io.IOException;

public class ModelBuilder {
    private final AnalyzerConfig config;

    public ModelBuilder(AnalyzerConfig config) {
        this.config = config;
    }

    public Model build() {
        try {
            final Model model = new Model();
            final ClassFileParser parser = new ClassFileParser();
            for (final File clazz : config.getClasses()) {
                parser.parse(clazz, model);
            }
            return model;
        } catch (IOException e) {
            throw new AnalyzerException("Problem executing ModelBuilder", e);
        }
    }
}
