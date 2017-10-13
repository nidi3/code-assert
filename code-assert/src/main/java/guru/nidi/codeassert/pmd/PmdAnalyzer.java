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
import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.renderers.Renderer;
import org.apache.commons.io.output.NullWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class PmdAnalyzer implements Analyzer<List<RuleViolation>> {
    private static final Comparator<RuleViolation> VIOLATION_SORTER = new Comparator<RuleViolation>() {
        @Override
        public int compare(RuleViolation v1, RuleViolation v2) {
            final int prio = v1.getRule().getPriority().getPriority() - v2.getRule().getPriority().getPriority();
            if (prio != 0) {
                return prio;
            }
            return v1.getRule().getName().compareTo(v2.getRule().getName());
        }
    };

    private final AnalyzerConfig config;
    private final PmdViolationCollector collector;
    private final Map<String, PmdRuleset> rulesets;

    public PmdAnalyzer(AnalyzerConfig config, PmdViolationCollector collector) {
        this(config, new HashMap<String, PmdRuleset>(), collector);
    }

    private PmdAnalyzer(AnalyzerConfig config, Map<String, PmdRuleset> rulesets, PmdViolationCollector collector) {
        this.config = config;
        this.collector = collector;
        this.rulesets = rulesets;
    }

    public PmdAnalyzer withRulesets(PmdRuleset... rulesets) {
        final Map<String, PmdRuleset> newRuleset = new HashMap<>();
        newRuleset.putAll(this.rulesets);
        for (final PmdRuleset ruleset : rulesets) {
            newRuleset.put(ruleset.name, ruleset);
        }
        return new PmdAnalyzer(config, newRuleset, collector);
    }

    public PmdAnalyzer withoutRulesets(PmdRuleset... rulesets) {
        final Map<String, PmdRuleset> newRuleset = new HashMap<>();
        newRuleset.putAll(this.rulesets);
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
        final PmdRenderer renderer = new PmdRenderer();
        final PMDConfiguration pmdConfig = createPmdConfig(renderer);
        PMD.doPMD(pmdConfig);
        return processViolations(renderer);
    }

    private PmdResult processViolations(PmdRenderer renderer) {
        final List<RuleViolation> violations = new ArrayList<>();
        final UsageCounter counter = new UsageCounter();
        if (renderer.getReport() != null) {
            for (final RuleViolation violation : renderer.getReport()) {
                if (counter.accept(collector.accept(violation))) {
                    violations.add(violation);
                }
            }
        }
        Collections.sort(violations, VIOLATION_SORTER);
        collector.printUnusedWarning(counter);
        return new PmdResult(this, violations, collector.unusedActions(counter));
    }

    private PMDConfiguration createPmdConfig(final PmdRenderer renderer) {
        final PMDConfiguration pmdConfig = new PMDConfiguration() {
            @Override
            public Renderer createRenderer() {
                for (final PmdRuleset ruleset : rulesets.values()) {
                    ruleset.apply(this);
                }
                return renderer;
            }
        };
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

    private static class PmdRenderer extends AbstractAccumulatingRenderer {
        PmdRenderer() {
            super("", "");
            super.setWriter(new NullWriter());
        }

        @Override
        public void setWriter(Writer writer) {
            //we want to keep NullWriter, no logging whatsoever, we are only interested in report
        }

        @Override
        public String defaultFileExtension() {
            return null;
        }

        @Override
        public void end() throws IOException {
            //do nothing
        }

        public Report getReport() {
            return report;
        }
    }
}
