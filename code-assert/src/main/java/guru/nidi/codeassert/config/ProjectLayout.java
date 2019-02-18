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

import guru.nidi.codeassert.config.AnalyzerConfig.Path;

import java.io.File;
import java.util.*;

import static guru.nidi.codeassert.config.Language.JAVA;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ProjectLayout<T extends ProjectLayout> {
    private final List<String> modules;
    private final EnumSet<Language> languages;

    protected ProjectLayout(List<String> modules, EnumSet<Language> languages) {
        this.modules = new ArrayList<>(modules);
        this.languages = languages;
    }

    protected ProjectLayout(List<String> modules, Language... languages) {
        this(modules, languages.length == 0 ? EnumSet.of(JAVA) : EnumSet.of(languages[0], languages));
    }

    public T modules(String... modules) {
        if (this.modules.size() > 1 || (this.modules.size() == 1 && this.modules.get(0) != null)) {
            throw new IllegalStateException("You already defined modules.");
        }
        this.modules.clear();
        this.modules.addAll(Arrays.asList(modules));
        return (T) this;
    }

    public EnumSet<Language> getLanguages() {
        return languages;
    }

    protected List<Path> path(String[] packs, String... paths) {
        final List<Path> res = new ArrayList<>();
        for (final String path : paths) {
            for (final String normPath : paths(path)) {
                if (packs.length == 0) {
                    res.add(new Path(normPath, ""));
                } else {
                    for (final String pack : packs) {
                        final String normPack = pack.replace('.', '/');
                        res.add(new Path(normPath, normPack));
                    }
                }
            }
        }
        return res;
    }

    private List<String> paths(String relative) {
        if (modules.isEmpty()) {
            return singletonList(relative);
        }
        return modules.stream().map(module ->
                module == null || module.length() == 0 || runningInModuleDir(module)
                        ? relative
                        : concat(module, relative)
        ).collect(toList());
    }

    private String concat(String path1, String path2) {
        return path1.endsWith("/") ? path1 + path2 : path1 + "/" + path2;
    }

    private boolean runningInModuleDir(String module) {
        return new File("").getAbsoluteFile().getName().equals(module);
    }

    public static class Maven extends ProjectLayout<Maven> {
        public Maven(String module, Language... languages) {
            super(singletonList(module), languages);
        }

        public AnalyzerConfig main(String... packages) {
            return new AnalyzerConfig(getLanguages(),
                    path(packages, "src/main/$language/"),
                    path(packages, "target/classes/"));
        }

        public AnalyzerConfig test(String... packages) {
            return new AnalyzerConfig(getLanguages(),
                    path(packages, "src/test/$language/"),
                    path(packages, "target/test-classes/"));
        }

        public AnalyzerConfig mainAndTest(String... packages) {
            return main(packages).and(test(packages));
        }
    }

    public static class Gradle extends ProjectLayout<Gradle> {
        public Gradle(String module, Language... languages) {
            super(singletonList(module), languages);
        }

        public AnalyzerConfig main(String... packages) {
            return new AnalyzerConfig(getLanguages(),
                    path(packages, "src/main/$language/"),
                    path(packages, "build/classes/main/", "build/classes/$language/main/", "out/production/classes"));
        }

        public AnalyzerConfig test(String... packages) {
            return new AnalyzerConfig(getLanguages(),
                    path(packages, "src/test/$language/"),
                    path(packages, "build/classes/test/", "build/classes/$language/test/", "out/test/classes"));
        }

        public AnalyzerConfig mainAndTest(String... packages) {
            return main(packages).and(test(packages));
        }
    }

}
