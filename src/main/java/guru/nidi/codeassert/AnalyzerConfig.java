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
package guru.nidi.codeassert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static guru.nidi.codeassert.util.ListUtils.merge;
import static guru.nidi.codeassert.util.ListUtils.prepend;

/**
 *
 */
public class AnalyzerConfig {
    private final List<String> sources;
    private final List<String> classes;
    private final PackageCollector collector;

    public AnalyzerConfig() {
        this(Collections.<String>emptyList(), Collections.<String>emptyList(), PackageCollector.all());
    }

    public AnalyzerConfig(AnalyzerConfig config) {
        this(config.sources, config.classes, config.collector);
    }

    protected AnalyzerConfig(List<String> sources, List<String> classes, PackageCollector collector) {
        this.sources = sources;
        this.classes = classes;
        this.collector = collector;
    }

    public static AnalyzerConfig mavenMainClasses(String... packages) {
        return new AnalyzerConfig(
                prepend("src/main/java/", Arrays.asList(packages)),
                prepend("target/classes/", Arrays.asList(packages)),
                PackageCollector.all());
    }

    public static AnalyzerConfig mavenTestClasses(String... packages) {
        return new AnalyzerConfig(
                prepend("src/test/java/", Arrays.asList(packages)),
                prepend("target/test-classes/", Arrays.asList(packages)),
                PackageCollector.all());
    }

    public static AnalyzerConfig mavenMainAndTestClasses(String... packages) {
        return new AnalyzerConfig(
                merge(prepend("src/main/java/", Arrays.asList(packages)), prepend("src/test/java/", Arrays.asList(packages))),
                merge(prepend("target/classes/", Arrays.asList(packages)), prepend("target/test-classes/", Arrays.asList(packages))),
                PackageCollector.all());
    }

    public AnalyzerConfig withSources(String... sources) {
        return new AnalyzerConfig(Arrays.asList(sources), classes, collector);
    }

    public AnalyzerConfig withClasses(String... classes) {
        return new AnalyzerConfig(sources, Arrays.asList(classes), collector);
    }

    public AnalyzerConfig collecting(PackageCollector collector) {
        return new AnalyzerConfig(sources, classes, collector);
    }

    public List<String> getSources() {
        return sources;
    }

    public List<String> getClasses() {
        return classes;
    }

    public PackageCollector getCollector() {
        return collector;
    }
}