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
package guru.nidi.codeassert.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static guru.nidi.codeassert.util.ListUtils.prepend;

/**
 *
 */
public class AnalyzerConfig {
    private final List<String> sources;
    private final List<String> classes;

    public AnalyzerConfig() {
        this(Collections.<String>emptyList(), Collections.<String>emptyList());
    }

    public AnalyzerConfig(AnalyzerConfig config) {
        this(config.sources, config.classes);
    }

    protected AnalyzerConfig(List<String> sources, List<String> classes) {
        this.sources = sources;
        this.classes = classes;
    }

    public static Maven maven() {
        return maven(null);
    }

    public static Maven maven(String module) {
        return new Maven(module);
    }

    public AnalyzerConfig withSources(String... sources) {
        return new AnalyzerConfig(Arrays.asList(sources), classes);
    }

    public AnalyzerConfig withClasses(String... classes) {
        return new AnalyzerConfig(sources, Arrays.asList(classes));
    }

    public List<String> getSources() {
        return sources;
    }

    public List<String> getClasses() {
        return classes;
    }

    public static class Maven {
        private final String module;

        public Maven(String module) {
            this.module = module;
        }

        public AnalyzerConfig main(String... packages) {
            return new AnalyzerConfig(
                    path(packages, "src/main/java/"),
                    path(packages, "target/classes/"));
        }

        public AnalyzerConfig test(String... packages) {
            return new AnalyzerConfig(
                    path(packages, "src/test/java/"),
                    path(packages, "target/test-classes/"));
        }

        public AnalyzerConfig mainAndTest(String... packages) {
            return new AnalyzerConfig(
                    path(packages, "src/main/java/", "src/test/java/"),
                    path(packages, "target/classes/", "target/test-classes/"));
        }

        private List<String> path(String[] packs, String... paths) {
            final List<String> res = new ArrayList<>();
            for (final String path : paths) {
                res.addAll(prepend(path(path), packs));
            }
            return res;
        }

        private String path(String relative) {
            if (module == null || module.length() == 0 || runningInModuleDir()) {
                return relative;
            }
            return module.endsWith("/")
                    ? module + relative
                    : module + "/" + relative;
        }

        private boolean runningInModuleDir() {
            return new File(".").getAbsolutePath().endsWith("/" + module + "/.");
        }
    }

}