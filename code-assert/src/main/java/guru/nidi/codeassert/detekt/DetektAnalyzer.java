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
package guru.nidi.codeassert.detekt;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.UsageCounter;
import io.gitlab.arturbosch.detekt.api.*;
import io.gitlab.arturbosch.detekt.api.internal.YamlConfig;
import io.gitlab.arturbosch.detekt.core.*;
import org.jetbrains.kotlin.config.JvmTarget;
import org.jetbrains.kotlin.config.LanguageVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static io.gitlab.arturbosch.detekt.api.Severity.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class DetektAnalyzer implements Analyzer<List<TypedDetektFinding>> {
    private static final Logger LOG = LoggerFactory.getLogger(DetektAnalyzer.class);
    private static final List<Severity> SEVERITIES = asList(
            Style, CodeSmell, Minor, Performance, Maintainability, Warning, Security, Defect);
    private static final Comparator<Severity> SEVERITY_COMPARATOR = Comparator.comparingInt(SEVERITIES::indexOf);
    private static final Comparator<TypedDetektFinding> FINDING_COMPARATOR = Comparator
            .comparing((TypedDetektFinding f) -> f.severity, SEVERITY_COMPARATOR)
            .thenComparing(f -> f.type)
            .thenComparing(f -> f.name);

    private final AnalyzerConfig config;
    private final DetektCollector collector;
    private final Config detektConfig;
    private final List<RuleSetProvider> ruleSetProviders;

    public DetektAnalyzer(AnalyzerConfig config, DetektCollector collector) {
        this(config, collector, null, emptyList());
    }

    private DetektAnalyzer(AnalyzerConfig config, DetektCollector collector, Config detektConfig,
                           List<RuleSetProvider> ruleSetProviders) {
        this.config = config;
        this.collector = collector;
        this.detektConfig = detektConfig;
        this.ruleSetProviders = ruleSetProviders;
    }

    public DetektAnalyzer withConfig(Config detektConfig) {
        return new DetektAnalyzer(config, collector, detektConfig, ruleSetProviders);
    }

    public DetektAnalyzer withRuleSets(RuleSetProvider... providers) {
        return new DetektAnalyzer(config, collector, detektConfig, asList(providers));
    }

    public DetektResult analyze() {
        final File baseDir = new File(AnalyzerConfig.Path.commonBase(config.getSourcePaths(KOTLIN)).getPath());
        try {
            final PrintStream printStream = new PrintStream(new LoggingOutputStream(), true, "utf-8");
            final ProcessingSettings settings = new ProcessingSettings(
                    singletonList(baseDir.toPath()), calcDetektConfig(), null, false, false, emptyList(), emptyList(),
                    LanguageVersion.KOTLIN_1_3, JvmTarget.JVM_1_8, Executors.newSingleThreadExecutor(),
                    printStream, printStream, false, false, emptyList());
            final DetektFacade df = DetektFacade.Companion.create(settings, ruleSetProviders(settings), emptyList());
            return createResult(baseDir, df.run());
        } catch (UnsupportedEncodingException e) {
            //cannot happen
            throw new AssertionError(e);
        }
    }

    private Config calcDetektConfig() {
        return new NoFormat(detektConfig == null
                ? YamlConfig.Companion.loadResource(DetektAnalyzer.class.getResource("default-detekt-config.yml"))
                : detektConfig);
    }

    private List<RuleSetProvider> ruleSetProviders(ProcessingSettings settings) {
        final List<RuleSetProvider> res = new RuleSetLocator(settings).load();
        res.addAll(ruleSetProviders);
        return res;
    }

    private DetektResult createResult(File baseDir, Detektion detektion) {
        final UsageCounter counter = new UsageCounter();
        final List<TypedDetektFinding> findings = detektion.getFindings().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(finding -> new TypedDetektFinding(baseDir, entry.getKey(), finding))
                        .filter(typed -> counter.accept(collector.accept(typed))))
                .sorted(FINDING_COMPARATOR)
                .collect(toList());
        collector.printUnusedWarning(counter);
        return new DetektResult(this, findings, collector.unusedActions(counter));
    }

    private static class NoFormat implements Config {
        private final Config delegate;

        NoFormat(Config delegate) {
            this.delegate = delegate;
        }

        @Override
        public Config subConfig(String s) {
            return delegate.subConfig(s);
        }

        @Override
        public <T> T valueOrDefault(String s, T t) {
            return "autoCorrect".equals(s) ? (T) Boolean.FALSE : delegate.valueOrDefault(s, t);
        }

        @Override
        public <T> T valueOrNull(String s) {
            return "autoCorrect".equals(s) ? (T) Boolean.FALSE : delegate.valueOrNull(s);
        }

        @Override
        public String getParentPath() {
            return delegate.getParentPath();
        }
    }

    private static class LoggingOutputStream extends OutputStream {
        private final byte[] buf = new byte[1024];
        private int pos;

        @Override
        public void write(int b) {
            buf[pos++] = (byte) b;
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            LOG.warn(new String(buf, 0, pos, StandardCharsets.UTF_8));
            pos = 0;
        }
    }
}
