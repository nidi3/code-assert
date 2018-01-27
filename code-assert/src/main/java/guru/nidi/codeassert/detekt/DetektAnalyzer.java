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
import io.gitlab.arturbosch.detekt.core.*;

import java.io.File;
import java.util.*;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static io.gitlab.arturbosch.detekt.api.Severity.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class DetektAnalyzer implements Analyzer<List<TypedDetektFinding>> {
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
        final ProcessingSettings settings = new ProcessingSettings(
                baseDir.toPath(), calcDetektConfig(), emptyList(), false, false, emptyList());
        final DetektFacade detekt = DetektFacade.Companion.create(settings, ruleSetProviders(settings), emptyList());
        return createResult(baseDir, detekt.run());
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
        final List<TypedDetektFinding> filtered = new ArrayList<>();
        final UsageCounter counter = new UsageCounter();
        for (final Map.Entry<String, List<Finding>> entry : detektion.getFindings().entrySet()) {
            for (final Finding finding : entry.getValue()) {
                final TypedDetektFinding typed = new TypedDetektFinding(baseDir, finding.getEntity(), entry.getKey(),
                        finding.getId(), finding.getIssue().getSeverity(), finding.getIssue().getDescription());
                if (counter.accept(collector.accept(typed))) {
                    filtered.add(typed);
                }
            }
        }
        collector.printUnusedWarning(counter);
        Collections.sort(filtered, TypedDetektFindingComparator.INSTANCE);
        return new DetektResult(this, filtered, collector.unusedActions(counter));
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
    }

    private static class SeverityComparator implements Comparator<Severity> {
        static final SeverityComparator INSTANCE = new SeverityComparator();

        private static final List<Severity> SEVERITIES = asList(
                Style, CodeSmell, Minor, Performance, Maintainability, Warning, Security, Defect);

        public int compare(Severity s1, Severity s2) {
            return SEVERITIES.indexOf(s2) - SEVERITIES.indexOf(s1);
        }
    }

    private static class TypedDetektFindingComparator implements Comparator<TypedDetektFinding> {
        static final TypedDetektFindingComparator INSTANCE = new TypedDetektFindingComparator();

        public int compare(TypedDetektFinding f1, TypedDetektFinding f2) {
            int res = SeverityComparator.INSTANCE.compare(f1.severity, f2.severity);
            if (res != 0) {
                return res;
            }
            res = f1.type.compareTo(f2.type);
            if (res != 0) {
                return res;
            }
            return f1.name.compareTo(f2.name);
        }
    }
}
