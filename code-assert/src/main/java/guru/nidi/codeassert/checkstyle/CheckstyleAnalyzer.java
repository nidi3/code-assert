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
package guru.nidi.codeassert.checkstyle;

import com.puppycrawl.tools.checkstyle.*;
import com.puppycrawl.tools.checkstyle.api.*;
import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.UsageCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public class CheckstyleAnalyzer implements Analyzer<List<AuditEvent>> {
    private static final Logger LOG = LoggerFactory.getLogger(CheckstyleAnalyzer.class);

    private final AnalyzerConfig config;
    private final StyleChecks checks;
    private final StyleEventCollector collector;

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
            LOG.warn(event.getFileName() + ":" + event.getLine() + " " + event.getMessage(), throwable);
        }
    }

    public CheckstyleAnalyzer(AnalyzerConfig config, StyleChecks checks, StyleEventCollector collector) {
        this.config = config;
        this.checks = checks;
        this.collector = collector;
    }

    public CheckstyleResult analyze() {
        final Checker checker = new Checker();
        try {
            final LoggingAuditListener listener = new LoggingAuditListener();
            checker.addListener(listener);
            checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
            checker.configure(ConfigurationLoader.loadConfiguration(checks.location, createPropertyResolver()));
            checker.process(config.getSources());
            return createResult(listener.events);
        } catch (CheckstyleException e) {
            throw new AnalyzerException("Problem executing Checkstyle.", e);
        } finally {
            checker.destroy();
        }
    }

    private PropertyResolver createPropertyResolver() {
        final Properties p = new Properties();
        for (final Map.Entry<String, Object> param : checks.params.entrySet()) {
            p.setProperty(param.getKey(), propertyValue(param.getKey(), param.getValue()));
        }
        return new PropertiesExpander(p);
    }

    private String propertyValue(String name, Object value) {
        if (name.endsWith("-tokens")) {
            final StringBuilder tokens = new StringBuilder("");
            for (final Integer val : (List<Integer>) value) {
                for (final Field f : TokenTypes.class.getFields()) {
                    try {
                        if (val.equals(f.get(null))) {
                            tokens.append(tokens.length() == 0 ? "" : ",").append(f.getName());
                        }
                    } catch (IllegalAccessException e) {
                        //ignore
                    }
                }
            }
            return tokens.toString();
        }
        return value.toString();
    }

    private CheckstyleResult createResult(List<AuditEvent> events) {
        final List<AuditEvent> sorted = new ArrayList<>(events);
        Collections.sort(sorted, EVENT_SORTER);
        final List<AuditEvent> filtered = new ArrayList<>();
        final UsageCounter counter = new UsageCounter();
        for (final AuditEvent event : sorted) {
            if (counter.accept(collector.accept(event))) {
                filtered.add(event);
            }
        }
        collector.printUnusedWarning(counter);
        return new CheckstyleResult(this, filtered, collector.unusedActions(counter));
    }

}
