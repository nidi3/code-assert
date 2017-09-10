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
package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.Analyzer;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.model.Model;
import guru.nidi.codeassert.model.Scope;

import java.util.*;

import static guru.nidi.codeassert.dependency.DependencyCollector.*;

public class DependencyAnalyzer implements Analyzer<Dependencies> {
    private static final String DUMMY_CLASS = ".DummyClass";

    private final Model model;
    private final DependencyRules rules;
    private final Scope scope;
    private final DependencyCollector collector;

    public DependencyAnalyzer(AnalyzerConfig config) {
        this(Model.from(config.getClasses()), DependencyRules.denyAll(), Scope.PACKAGES, new DependencyCollector());
    }

    public DependencyAnalyzer(Model model) {
        this(model, DependencyRules.denyAll(), Scope.PACKAGES, new DependencyCollector());
    }

    private DependencyAnalyzer(Model model, DependencyRules rules, Scope scope, DependencyCollector collect) {
        this.model = model;
        this.rules = rules;
        this.scope = scope;
        this.collector = collect;
    }

    public DependencyAnalyzer rules(DependencyRules rules) {
        return new DependencyAnalyzer(model, rules, scope, collector);
    }

    public DependencyAnalyzer scope(Scope scope) {
        return new DependencyAnalyzer(model, rules, scope, collector);
    }

    public DependencyAnalyzer collector(DependencyCollector collector) {
        return new DependencyAnalyzer(model, rules, scope, collector);
    }

    @Override
    public DependencyResult analyze() {
        final Dependencies dependencies = rules.analyzeRules(scope.in(model));
        final UsageCounter counter = new UsageCounter();
        final Dependencies filtered = new Dependencies(new DependencyMap(), new DependencyMap(), new DependencyMap(),
                handleNotExisting(dependencies, counter),
                handleUndefined(dependencies, counter),
                handleCycles(dependencies, counter));
        handleMissing(dependencies, counter, filtered);
        handleDenied(dependencies, counter, filtered);
        collector.printUnusedWarning(counter);
        return new DependencyResult(this, filtered, collector.unusedActions(counter));
    }

    private void handleMissing(Dependencies dependencies, UsageCounter counter, Dependencies filtered) {
        for (final String name : dependencies.missing.getElements()) {
            if (counter.accept(collector.accept(new DependencyEntry(MISSING, className(name))))) {
                filtered.missing.with(name, dependencies.missing);
            }
        }
    }

    private void handleDenied(Dependencies dependencies, UsageCounter counter, Dependencies filtered) {
        for (final String name : dependencies.denied.getElements()) {
            if (counter.accept(collector.accept(new DependencyEntry(DENIED, className(name))))) {
                filtered.denied.with(name, dependencies.denied);
            }
        }
    }

    private Set<DependencyMap> handleCycles(Dependencies dependencies, UsageCounter counter) {
        final Set<DependencyMap> res = new HashSet<>();
        for (final DependencyMap cycle : dependencies.cycles) {
            final DependencyMap map = new DependencyMap();
            for (final String from : cycle.getElements()) {
                if (counter.accept(collector.accept(new DependencyEntry(CYCLE, className(from))))) {
                    for (final Map.Entry<String, DependencyMap.Info> to : cycle.getDependencies(from).entrySet()) {
                        if (counter.accept(collector.accept(new DependencyEntry(CYCLE, className(to.getKey()))))) {
                            map.with(to.getValue().getSpecificity(), from, to.getValue().getVias(), to.getKey());
                        }
                    }
                }
            }
            if (!map.isEmpty()) {
                res.add(map);
            }
        }
        return res;
    }

    private Set<String> handleUndefined(Dependencies dependencies, UsageCounter counter) {
        final Set<String> res = new HashSet<>();
        for (final String name : dependencies.undefined) {
            if (counter.accept(collector.accept(new DependencyEntry(UNDEFINED, className(name))))) {
                res.add(name);
            }
        }
        return res;
    }

    private Set<LocationMatcher> handleNotExisting(Dependencies dependencies, UsageCounter counter) {
        final Set<LocationMatcher> res = new HashSet<>();
        for (final LocationMatcher name : dependencies.notExisting) {
            if (counter.accept(collector.accept(new DependencyEntry(NOT_EXISTING, className(name))))) {
                res.add(name);
            }
        }
        return res;
    }

    private String className(Object name) {
        //TODO not nice
        return scope instanceof Scope.Packages ? name + DUMMY_CLASS : name.toString();
    }
}
