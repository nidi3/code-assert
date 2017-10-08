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
package guru.nidi.codeassert.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectLayout {
    private final String module;

    protected ProjectLayout(String module) {
        this.module = module;
    }

    protected List<AnalyzerConfig.Path> path(String[] packs, String... paths) {
        final List<AnalyzerConfig.Path> res = new ArrayList<>();
        for (final String path : paths) {
            final String normPath = path(path);
            if (packs.length == 0) {
                res.add(new AnalyzerConfig.Path(normPath, ""));
            } else {
                for (final String pack : packs) {
                    final String normPack = pack.replace('.', '/');
                    res.add(new AnalyzerConfig.Path(normPath, normPack));
                }
            }
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
        return new File("").getAbsoluteFile().getName().equals(module);
    }

    public static class Maven extends ProjectLayout {
        public Maven(String module) {
            super(module);
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
            return main(packages).and(test(packages));
        }
    }

    public static class Gradle extends ProjectLayout {
        public Gradle(String module) {
            super(module);
        }

        public AnalyzerConfig main(String... packages) {
            return new AnalyzerConfig(
                    path(packages, "src/main/java/"),
                    path(packages, "build/classes/main/", "build/classes/java/main/", "out/production/classes"));
        }

        public AnalyzerConfig test(String... packages) {
            return new AnalyzerConfig(
                    path(packages, "src/test/java/"),
                    path(packages, "build/classes/test/", "build/classes/java/test/", "out/test/classes"));
        }

        public AnalyzerConfig mainAndTest(String... packages) {
            return main(packages).and(test(packages));
        }
    }

}
