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

import guru.nidi.codeassert.config.ProjectLayout.Gradle;
import guru.nidi.codeassert.config.ProjectLayout.Maven;

import java.io.File;
import java.util.*;

import static guru.nidi.codeassert.util.ListUtils.concat;

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

    public AnalyzerConfig and(AnalyzerConfig config) {
        return new AnalyzerConfig(concat(sources, config.sources), concat(classes, config.classes));
    }

    public static Maven maven() {
        return maven(null);
    }

    public static Maven maven(String module) {
        return new Maven(module);
    }

    public static Gradle gradle() {
        return gradle(null);
    }

    public static Gradle gradle(String module) {
        return new Gradle(module);
    }

    public AnalyzerConfig withSources(File basedir, String... packages) {
        return new AnalyzerConfig(concat(sources, Path.of(basedir, packages)), classes);
    }

    public AnalyzerConfig withClasses(File basedir, String... packages) {
        return new AnalyzerConfig(sources, concat(classes, Path.of(basedir, packages)));
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
