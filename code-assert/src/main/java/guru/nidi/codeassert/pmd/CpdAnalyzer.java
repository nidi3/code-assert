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
import net.sourceforge.pmd.cpd.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class CpdAnalyzer implements Analyzer<List<Match>> {
    private static final Comparator<Match> MATCH_SORTER = Comparator
            .comparingInt(Match::getTokenCount).reversed()
            .thenComparing(m -> m.getFirstMark().getFilename());

    private final AnalyzerConfig config;
    private final int minTokens;
    private final CpdMatchCollector collector;

    public CpdAnalyzer(AnalyzerConfig config, int minTokens, CpdMatchCollector collector) {
        this.config = config;
        this.minTokens = minTokens;
        this.collector = collector;
    }

    @Override
    public CpdResult analyze() {
        final CPD cpd = createCpd();
        cpd.go();
        return processMatches(cpd.getMatches());
    }

    private CPD createCpd() {
        final CPD cpd = new CPD(createCpdConfig());
        for (final AnalyzerConfig.Path source : config.getSourcePaths()) {
            try {
                cpd.addRecursively(new File(source.getPath()));
            } catch (IOException e) {
                throw new AnalyzerException("Problem reading directory '" + source + "'", e);
            }
        }
        return cpd;
    }

    private CPDConfiguration createCpdConfig() {
        final CPDConfiguration cpdConfig = new CPDConfiguration();
        cpdConfig.setMinimumTileSize(minTokens);
        cpdConfig.postContruct();
        return cpdConfig;
    }

    private CpdResult processMatches(Iterator<Match> matches) {
        final UsageCounter counter = new UsageCounter();
        final Iterable<Match> miter = () -> matches;
        final List<Match> res = StreamSupport.stream(miter.spliterator(), false)
                .filter(m -> counter.accept(collector.accept(m)))
                .sorted(MATCH_SORTER)
                .collect(toList());
        collector.printUnusedWarning(counter);
        return new CpdResult(this, res, collector.unusedActions(counter));
    }
}
