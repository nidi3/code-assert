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
package guru.nidi.codeassert.pmd;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.UsageCounter;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.LanguageFilenameFilter;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.processor.MonoThreadProcessor;
import net.sourceforge.pmd.processor.MultiThreadProcessor;
import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.util.FileUtil;
import net.sourceforge.pmd.util.datasource.DataSource;
import org.apache.commons.io.output.NullWriter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class PmdAnalyzer implements Analyzer<List<RuleViolation>> {
    static {
        JavaUtilLoggerConfigurer.init();
    }

    private static final Comparator<RuleViolation> VIOLATION_SORTER = Comparator
            .comparingInt((RuleViolation v) -> v.getRule().getPriority().getPriority())
            .thenComparing(v -> v.getRule().getName());

    private final AnalyzerConfig config;
    private final PmdViolationCollector collector;
    private final Map<String, PmdRuleset> rulesets;

    public PmdAnalyzer(AnalyzerConfig config, PmdViolationCollector collector) {
        this(config, new HashMap<>(), collector);
    }

    private PmdAnalyzer(AnalyzerConfig config, Map<String, PmdRuleset> rulesets, PmdViolationCollector collector) {
        this.config = config;
        this.collector = collector;
        this.rulesets = rulesets;
    }

    public PmdAnalyzer withRulesets(PmdRuleset... rulesets) {
        final Map<String, PmdRuleset> newRuleset = new HashMap<>(this.rulesets);
        for (final PmdRuleset ruleset : rulesets) {
            newRuleset.put(ruleset.name, ruleset);
        }
        return new PmdAnalyzer(config, newRuleset, collector);
    }

    public PmdAnalyzer withoutRulesets(PmdRuleset... rulesets) {
        final Map<String, PmdRuleset> newRuleset = new HashMap<>(this.rulesets);
        for (final PmdRuleset ruleset : rulesets) {
            newRuleset.remove(ruleset.name);
        }
        return new PmdAnalyzer(config, newRuleset, collector);
    }

    @Override
    public PmdResult analyze() {
        if (rulesets.isEmpty()) {
            throw new AnalyzerException("No rulesets defined. Use the withRulesets methods to define some. "
                    + "See Rulesets class for predefined rule sets.");
        }
        final PMDConfiguration pmdConfig = createPmdConfig();
        final RuleSetFactory ruleSetFactory = createRuleSetFactory(pmdConfig);
        final List<DataSource> files = FileUtil.collectFiles(pmdConfig.getInputPaths(),
                new LanguageFilenameFilter(new JavaLanguageModule()));

        return runPmd(pmdConfig, ruleSetFactory, files);
    }

    private PMDConfiguration createPmdConfig() {
        final PMDConfiguration pmdConfig = new PMDConfiguration();
        final StringBuilder inputs = new StringBuilder();
        for (final AnalyzerConfig.Path source : config.getSourcePaths()) {
            inputs.append(',').append(source.getPath());
        }
        pmdConfig.setInputPaths(inputs.substring(1));
        pmdConfig.setRuleSets(ruleSetNames());
        pmdConfig.setThreads(0);
        return pmdConfig;
    }

    private String ruleSetNames() {
        final StringBuilder s = new StringBuilder();
        for (final PmdRuleset ruleset : rulesets.values()) {
            s.append(',').append(ruleset.name);
        }
        return rulesets.isEmpty() ? "" : s.substring(1);
    }

    private RuleSetFactory createRuleSetFactory(PMDConfiguration pmdConfig) {
        return new RuleSetFactory(PmdAnalyzer.class.getClassLoader(), pmdConfig.getMinimumPriority(), true,
                pmdConfig.isRuleSetFactoryCompatibilityEnabled()) {
            @Override
            public synchronized RuleSets createRuleSets(List<RuleSetReferenceId> ruleSetReferenceIds)
                    throws RuleSetNotFoundException {
                final RuleSets sets = super.createRuleSets(ruleSetReferenceIds);
                for (final PmdRuleset ruleset : rulesets.values()) {
                    ruleset.apply(sets);
                }
                return sets;
            }
        };
    }

    private PmdResult runPmd(PMDConfiguration pmdConfig, RuleSetFactory ruleSetFactory, List<DataSource> files) {
        try {
            final PmdRenderer renderer = new PmdRenderer();
            renderer.start();
            final RuleContext ctx = new RuleContext();
            if (pmdConfig.getThreads() > 0) {
                new MultiThreadProcessor(pmdConfig).processFiles(ruleSetFactory, files, ctx, singletonList(renderer));
            } else {
                new MonoThreadProcessor(pmdConfig).processFiles(ruleSetFactory, files, ctx, singletonList(renderer));
            }
            renderer.end();
            renderer.flush();
            return processViolations(renderer);
        } catch (IOException e) {
            throw new AnalyzerException("Problem running PMD", e);
        }
    }

    private PmdResult processViolations(PmdRenderer renderer) {
        final UsageCounter counter = new UsageCounter();
        final List<RuleViolation> violations = asStream(renderer.getReport())
                .filter(v -> counter.accept(collector.accept(v)))
                .sorted(VIOLATION_SORTER)
                .collect(toList());
        collector.printUnusedWarning(counter);
        return new PmdResult(this, violations, collector.unusedActions(counter));
    }

    private Stream<RuleViolation> asStream(Report report) {
        return report == null ? Stream.empty() : StreamSupport.stream(report.spliterator(), false);
    }

    private static class PmdRenderer extends AbstractAccumulatingRenderer {
        PmdRenderer() {
            super("", "");
            super.setWriter(new NullWriter());
        }

        @Override
        public String defaultFileExtension() {
            return null;
        }

        @Override
        public void end() {
            //do nothing
        }

        public Report getReport() {
            return report;
        }
    }
}
