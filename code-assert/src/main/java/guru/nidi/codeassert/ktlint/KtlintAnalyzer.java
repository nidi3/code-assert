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
package guru.nidi.codeassert.ktlint;

import com.github.shyiko.ktlint.core.*;
import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.config.UsageCounter;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static guru.nidi.codeassert.config.Language.KOTLIN;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class KtlintAnalyzer implements Analyzer<List<LocatedLintError>> {
    private static final Logger LOG = LoggerFactory.getLogger(KtlintAnalyzer.class);

    private final AnalyzerConfig config;
    private final KtlintCollector collector;
    private final List<RuleSet> ruleSets;

    public KtlintAnalyzer(AnalyzerConfig config, KtlintCollector collector) {
        this(config, collector, Collections.<RuleSet>emptyList());
    }

    private KtlintAnalyzer(AnalyzerConfig config, KtlintCollector collector, List<RuleSet> ruleSets) {
        this.config = config;
        this.collector = collector;
        this.ruleSets = ruleSets;
    }

    public KtlintAnalyzer withRuleSets(RuleSet... ruleSets) {
        return new KtlintAnalyzer(config, collector, asList(ruleSets));
    }

    public KtlintResult analyze() {
        final ErrorListener listener = new ErrorListener();
        for (final File src : config.getSources(KOTLIN)) {
            try {
                listener.currentFile = src;
                KtLint.INSTANCE.lint(readFile(src), findRuleSets(), listener);
            } catch (IOException e) {
                LOG.error("Could not read file {}", src, e);
            }
        }
        return createResult(listener);
    }

    private List<RuleSet> findRuleSets() {
        if (!ruleSets.isEmpty()) {
            return ruleSets;
        }
        final List<RuleSet> res = new ArrayList<>();
        for (final RuleSetProvider provider : ServiceLoader.load(RuleSetProvider.class)) {
            final RuleSet ruleSet = provider.get();
            if ("standard".equals(ruleSet.getId())) {
                res.add(0, ruleSet);
            } else {
                res.add(ruleSet);
            }
        }
        return res;
    }

    private String readFile(File f) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private KtlintResult createResult(ErrorListener listener) {
        final List<LocatedLintError> filtered = new ArrayList<>();
        final UsageCounter counter = new UsageCounter();
        for (final LocatedLintError error : listener.errors) {
            if (counter.accept(collector.accept(error))) {
                filtered.add(error);
            }
        }
        collector.printUnusedWarning(counter);
        return new KtlintResult(this, filtered, collector.unusedActions(counter));
    }

    private static class ErrorListener implements Function1<LintError, Unit> {
        final List<LocatedLintError> errors = new ArrayList<>();
        File currentFile;

        @Override
        public Unit invoke(LintError e) {
            errors.add(new LocatedLintError(currentFile, e.getLine(), e.getRuleId(), e.getDetail()));
            return null;
        }
    }
}
