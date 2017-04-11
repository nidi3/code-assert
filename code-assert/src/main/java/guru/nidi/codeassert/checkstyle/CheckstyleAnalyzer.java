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
package guru.nidi.codeassert.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.UsageCounter;

import java.io.File;
import java.util.*;

public class CheckstyleAnalyzer implements Analyzer<List<AuditEvent>> {
    private static final Comparator<AuditEvent> EVENT_SORTER = new Comparator<AuditEvent>() {
        @Override
        public int compare(AuditEvent b1, AuditEvent b2) {
            final int severity = b1.getSeverityLevel().compareTo(b2.getSeverityLevel());
            if (severity != 0) {
                return severity;
            }
            return b1.getLocalizedMessage().getKey().compareTo(b2.getLocalizedMessage().getKey());
        }
    };

    private static class LoggingAuditListener implements AuditListener {
        final List<AuditEvent> events = new ArrayList<>();

        @Override
        public void auditStarted(AuditEvent event) {
        }

        @Override
        public void auditFinished(AuditEvent event) {
        }

        @Override
        public void fileStarted(AuditEvent event) {
        }

        @Override
        public void fileFinished(AuditEvent event) {
        }

        @Override
        public void addError(AuditEvent event) {
            events.add(event);
        }

        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            System.out.println(event);
        }
    }


    private final AnalyzerConfig config;
    private final StyleEventCollector collector;

    public CheckstyleAnalyzer(AnalyzerConfig config, StyleEventCollector collector) {
        this.config = config;
        this.collector = collector;
    }

    public CheckstyleResult analyze() {
        final Checker checker = new Checker();
        try {
            final LoggingAuditListener listener = new LoggingAuditListener();
            checker.addListener(listener);
            checker.setModuleClassLoader(CheckstyleAnalyzer.class.getClassLoader());
            checker.configure(ConfigurationLoader.loadConfiguration(
                    "/google_checks.xml", new PropertiesExpander(new Properties())));
            checker.setFileExtensions(".java");
            checker.process(inputFiles());
            return createResult(listener.events);
        } catch (CheckstyleException e) {
            throw new AnalyzerException("Problem executing Checkstyle.", e);
        } finally {
            checker.destroy();
        }
    }

    private List<File> inputFiles() {
        final List<File> files = new ArrayList<>();
        for (final AnalyzerConfig.Path path : config.getSources()) {
            crawlDir(new File(path.getPath()), files);
        }
        return files;
    }

    private void crawlDir(File dir, List<File> files) {
        final File[] fs = dir.listFiles();
        if (fs != null) {
            for (final File f : fs) {
                if (f.isFile()) {
                    files.add(f);
                }
                if (f.isDirectory()) {
                    crawlDir(f, files);
                }
            }
        }
    }

    private CheckstyleResult createResult(List<AuditEvent> events) {
        final List<AuditEvent> sorted = new ArrayList<>(events);
        Collections.sort(sorted, EVENT_SORTER);
        final List<AuditEvent> filtered = new ArrayList<>();
        final UsageCounter counter = new UsageCounter();
        for (final AuditEvent bug : sorted) {
            if (counter.accept(collector.accept(bug))) {
                filtered.add(bug);
            }
        }
        collector.printUnusedWarning(counter);
        return new CheckstyleResult(this, filtered, collector.unusedActions(counter));
    }

}
