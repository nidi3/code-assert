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

import static guru.nidi.codeassert.config.Language.JAVA;
import static guru.nidi.codeassert.util.ListUtils.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class AnalyzerConfig {
    private final EnumSet<Language> languages;
    private final List<Path> sources;
    private final List<Path> classes;

    public AnalyzerConfig() {
        this(EnumSet.of(JAVA), Collections.<Path>emptyList(), Collections.<Path>emptyList());
    }

    public AnalyzerConfig(AnalyzerConfig config) {
        this(config.languages, config.sources, config.classes);
    }

    protected AnalyzerConfig(EnumSet<Language> languages, List<Path> sources, List<Path> classes) {
        this.languages = languages;
        this.sources = sources;
        this.classes = classes;
    }

    public AnalyzerConfig and(AnalyzerConfig config) {
        return new AnalyzerConfig(
                concat(languages, config.languages),
                concat(sources, config.sources),
                concat(classes, config.classes));
    }

    public static Maven maven(Language... languages) {
        return maven(null, languages);
    }

    public static Maven maven(String module, Language... languages) {
        return new Maven(module, languages);
    }

    public static Gradle gradle(Language... languages) {
        return gradle(null, languages);
    }

    public static Gradle gradle(String module, Language... languages) {
        return new Gradle(module, languages);
    }

    public AnalyzerConfig withSources(File basedir, String... packages) {
        return new AnalyzerConfig(languages, concat(sources, Path.of(basedir, packages)), classes);
    }

    public AnalyzerConfig withClasses(File basedir, String... packages) {
        return new AnalyzerConfig(languages, sources, concat(classes, Path.of(basedir, packages)));
    }

    public List<Path> getSourcePaths(Language... languages) {
        return getPaths(sources, languages);
    }

    public List<Path> getClassPaths(Language... languages) {
        return getPaths(classes, languages);
    }

    public List<File> getSources(Language... languages) {
        return getFiles(sources, null, languages);
    }

    public List<File> getClasses(Language... languages) {
        return getFiles(classes, ".class", languages);
    }

    private List<Path> getPaths(List<Path> paths, Language... languages) {
        final Set<Path> res = new HashSet<>();
        for (final Language language : calcLanguages(languages)) {
            for (final Path path : paths) {
                res.add(path.forLanguage(language));
            }
        }
        return new ArrayList<>(res);
    }

    private List<File> getFiles(List<Path> paths, String suffix, Language... languages) {
        final List<File> files = new ArrayList<>();
        for (final Language language : calcLanguages(languages)) {
            final List<String> suff = suffix == null ? language.suffices : singletonList(suffix);
            for (final Path path : paths) {
                crawlDir(new File(path.forLanguage(language).getPath()), suff, files);
            }
        }
        return files;
    }

    private EnumSet<Language> calcLanguages(Language... languages) {
        final EnumSet<Language> res = EnumSet.copyOf(this.languages);
        if (languages.length > 0) {
            res.retainAll(asList(languages));
        }
        return res;
    }

    private void crawlDir(File base, List<String> suffices, List<File> res) {
        final File[] files = base.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isFile() && hasAnySuffix(file.getName(), suffices)) {
                    res.add(file);
                }
                if (file.isDirectory()) {
                    crawlDir(file, suffices, res);
                }
            }
        }
    }

    private boolean hasAnySuffix(String s, List<String> suffices) {
        for (final String suffix : suffices) {
            if (s.endsWith(suffix)) {
                return true;
            }
        }
        return false;
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

        public Path forLanguage(Language language) {
            return new Path(base.replace("$language", language.path), pack);
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

        public Path commonBase(Path path) {
            final String[] thisParts = getPath().split("/");
            final String[] otherParts = path.getPath().split("/");
            int i = 0;
            final StringBuilder res = new StringBuilder();
            while (i < Math.min(thisParts.length, otherParts.length) && thisParts[i].equals(otherParts[i])) {
                res.append(thisParts[i]).append('/');
                i++;
            }
            return res.length() < base.length()
                    ? new Path(res.substring(0, res.length() - 1), "")
                    : new Path(base, pack.substring(0, res.length() - base.length() - 2));
        }

        public static Path commonBase(Iterable<Path> paths) {
            Path base = null;
            for (final Path path : paths) {
                base = base == null ? path : base.commonBase(path);
            }
            return base;
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
