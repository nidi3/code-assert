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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.config.UserPreferences;
import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.UsageCounter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class FindBugsAnalyzer implements Analyzer<List<BugInstance>> {
    private static final Comparator<BugInstance> BUG_COMPARATOR = Comparator
            .comparingInt(BugInstance::getPriority)
            .thenComparingInt(BugInstance::getBugRank)
            .thenComparing(BugInstance::getType);

    final AnalyzerConfig config;
    private final BugCollector collector;

    public FindBugsAnalyzer(AnalyzerConfig config, BugCollector collector) {
        this.config = config;
        this.collector = collector;
    }

    public FindBugsResult analyze() {
        final Project project = createProject();
        final BugCollectionBugReporter bugReporter = createReporter(project);
        final FindBugs2 findBugs = createFindBugs(project, bugReporter);
        try {
            findBugs.execute();
        } catch (IOException | InterruptedException e) {
            throw new AnalyzerException("Problem executing FindBugs.", e);
        }
        return createBugList(bugReporter);
    }

    private Project createProject() {
        final Project project = new Project();
        PluginLoader.addPluginsTo(project);

        for (final AnalyzerConfig.Path clazz : config.getClassPaths()) {
            project.addFile(clazz.getPath());
        }
        project.addSourceDirs(config.getSourcePaths().stream().map(AnalyzerConfig.Path::getPath).collect(toList()));
        final String pathSeparator = System.getProperty("path.separator");
        final String classPath = System.getProperty("java.class.path");
        for (final String entry : classPath.split(pathSeparator)) {
            project.addAuxClasspathEntry(entry);
        }
        return project;
    }

    private BugCollectionBugReporter createReporter(Project project) {
        final BugCollectionBugReporter bugReporter = new BugCollectionBugReporter(project);
        bugReporter.setPriorityThreshold(Priorities.LOW_PRIORITY);
        bugReporter.setApplySuppressions(true);
        return bugReporter;
    }

    private FindBugs2 createFindBugs(Project project, BugCollectionBugReporter bugReporter) {
        final FindBugs2 findBugs = new FindBugs2();
        findBugs.setProject(project);
        findBugs.setBugReporter(bugReporter);
        findBugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance());
        findBugs.setUserPreferences(UserPreferences.createDefaultUserPreferences());
        return findBugs;
    }

    private FindBugsResult createBugList(BugCollectionBugReporter bugReporter) {
        final UsageCounter counter = new UsageCounter();
        final List<BugInstance> bugs = bugReporter.getBugCollection().getCollection().stream()
                .filter(b -> counter.accept(collector.accept(b)))
                .sorted(BUG_COMPARATOR)
                .collect(toList());
        collector.printUnusedWarning(counter);
        return new FindBugsResult(this, bugs, collector.unusedActions(counter));
    }
}
