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
package guru.nidi.codeassert.jacoco;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.UsageCounter;
import guru.nidi.codeassert.config.ValuedLocation;

import java.io.*;
import java.util.*;

import static java.util.Collections.singleton;

public class JacocoAnalyzer implements Analyzer<List<ValuedLocation>> {
    private final File jacocoCsv;
    private final CoverageCollector collector;

    public JacocoAnalyzer(CoverageCollector collector) {
        this(new File("target/site/jacoco/jacoco.csv"), collector);
    }

    public JacocoAnalyzer(File jacocoCsv, CoverageCollector collector) {
        this.jacocoCsv = jacocoCsv.isDirectory() ? new File(jacocoCsv, "jacoco.csv") : jacocoCsv;
        this.collector = collector;
        if (!this.jacocoCsv.exists()) {
            throw new AnalyzerException("Coverage information in '" + jacocoCsv + "' does not exist.");
        }
    }

    @Override
    public JacocoResult analyze() {
        final Coverages coverages = readReport();
        return filterResult(coverages);
    }

    private Coverages readReport() {
        final Coverages coverages = new Coverages();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(jacocoCsv), "utf-8"))) {
            String line;
            in.readLine();
            while ((line = in.readLine()) != null) {
                final String[] parts = line.split(",");
                coverages.add(new Coverage(parts[1], parts[2],
                        Integer.parseInt(parts[3]), Integer.parseInt(parts[4]),
                        Integer.parseInt(parts[5]), Integer.parseInt(parts[6]),
                        Integer.parseInt(parts[7]), Integer.parseInt(parts[8]),
                        Integer.parseInt(parts[9]), Integer.parseInt(parts[10]),
                        Integer.parseInt(parts[11]), Integer.parseInt(parts[12])));
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            throw new AnalyzerException("Problem analyzing coverage", e);
        }
        return coverages;
    }

    private JacocoResult filterResult(Coverages coverages) {
        final List<ValuedLocation> filtered = new ArrayList<>();
        final UsageCounter counter = new UsageCounter();
        filter(filtered, singleton(coverages.global), counter);
        filter(filtered, coverages.perPackage.values(), counter);
        filter(filtered, coverages.coverages, counter);
        collector.printUnusedWarning(counter);
        return new JacocoResult(this, filtered, collector.unusedActions(counter), collector.types);
    }

    private void filter(List<ValuedLocation> filtered, Collection<Coverage> coverages, UsageCounter counter) {
        for (final Coverage c : coverages) {
            final ValuedLocation vc = c.toValuedLocation(collector.types);
            if (counter.accept(collector.accept(vc))) {
                filtered.add(vc);
            }
        }
    }
}
