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
import java.util.*;

public class AnalyzerConfig {
    private final List<Path> sources;
    private final List<Path> classes;

    public AnalyzerConfig() {
        this(Collections.<Path>emptyList(), Collections.<Path>emptyList());
    }

    public AnalyzerConfig(AnalyzerConfig config) {
        this(config.sources, config.classes);
    }

    protected AnalyzerConfig(List<Path> sources, List<Path> classes) {
        this.sources = sources;
        this.classes = classes;
    }

    public static Maven maven() {
        return maven(null);
    }

    public static Maven maven(String module) {
        return new Maven(module);
    }

    public AnalyzerConfig withSources(File basedir, String... packages) {
        return new AnalyzerConfig(join(sources, Path.of(basedir, packages)), classes);
    }

    public AnalyzerConfig withClasses(File basedir, String... packages) {
        return new AnalyzerConfig(sources, join(classes, Path.of(basedir, packages)));
    }

    public List<Path> getSourcePaths() {
        return sources;
    }

    public List<Path> getClassPaths() {
        return classes;
    }

    public List<File> getSources() {
        final List<File> files = new ArrayList<>();
        for (final Path source : sources) {
            crawlDir(new File(source.getPath()), ".java", files);
        }
        return files;
    }

    public List<File> getClasses() {
        final List<File> files = new ArrayList<>();
        for (final Path clazz : classes) {
            crawlDir(new File(clazz.getPath()), ".class", files);
        }
        return files;
    }

    private void crawlDir(File base, String suffix, List<File> res) {
        final File[] files = base.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isFile() && file.getName().endsWith(suffix)) {
                    res.add(file);
                }
                if (file.isDirectory()) {
                    crawlDir(file, suffix, res);
                }
            }
        }
    }

    private List<Path> join(List<Path> p1, List<Path> p2) {
        final List<Path> res = new ArrayList<>(p1);
        res.addAll(p2);
        return res;
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

        private List<Path> path(String[] packs, String... paths) {
            final List<Path> res = new ArrayList<>();
            for (final String path : paths) {
                final String normPath = path(path);
                if (packs.length == 0) {
                    res.add(new Path(normPath, ""));
                } else {
                    for (final String pack : packs) {
                        final String normPack = pack.replace('.', '/');
                        res.add(new Path(normPath, normPack));
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
            return new File(".").getAbsolutePath().endsWith("/" + module + "/.");
        }
    }

    public static class Path {
        private final String base;
        private final String pack;

        public Path(String base, String pack) {
            this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
            this.pack = pack.startsWith("/") ? pack.substring(1) : pack;
        }

        public static List<Path> of(File basedir, String... packages) {
            final List<Path> sources = new ArrayList<>();
            if (packages.length == 0) {
                sources.add(new Path(basedir.getPath(), ""));
            } else {
                for (final String pack : packages) {
                    sources.add(new Path(basedir.getPath(), pack));
                }
            }
            return sources;
        }

        public String getPath() {
            return base + (pack.length() == 0 ? "" : ("/" + pack));
        }

        public String getBase() {
            return base;
        }

        public String getPack() {
            return pack;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Path path = (Path) o;

            if (!base.equals(path.base)) {
                return false;
            }
            return pack.equals(path.pack);

        }

        @Override
        public int hashCode() {
            int result = base.hashCode();
            result = 31 * result + pack.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Path(" + base + ", " + pack + ")";
        }
    }
}
