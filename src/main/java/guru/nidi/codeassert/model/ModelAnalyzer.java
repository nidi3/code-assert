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
package guru.nidi.codeassert.model;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.AnalyzerConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class ModelAnalyzer implements Analyzer<Collection<JavaPackage>> {
    private final AnalyzerConfig config;

    public ModelAnalyzer(AnalyzerConfig config) {
        this.config = config;
    }

    public ModelResult analyze() {
        try {
            final JavaClassBuilder builder = new JavaClassBuilder(
                    new ClassFileParser(),
                    new FileManager().withDirectories(config.getClasses()));
            builder.build();
            return new ModelResult(this, builder.packages.values(), Collections.<String>emptyList());
        } catch (IOException e) {
            throw new AnalyzerException("Problem executing ModelAnalyzer", e);
        }
    }
}
