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

import java.util.HashSet;
import java.util.Set;

import static guru.nidi.codeassert.dependency.DependencyCollector.*;

public class DependencyAnalyzer implements Analyzer<Dependencies> {
    private static final String DUMMY_CLASS = ".DummyClass";

    private final AnalyzerConfig config;
    private final DependencyRules rules;
    private final Scope scope;
    private final DependencyCollector collector;

    public DependencyAnalyzer(AnalyzerConfig config, DependencyRules rules, Scope scope, DependencyCollector collect) {
        this.config = config;
        this.rules = rules;
        this.scope = scope;
        this.collector = collect;
    }

    @Override
    public DependencyResult analyze() {
        final Model model = Model.from(config.getClasses());
        final Dependencies dependencies = rules.analyzeRules(scope.in(model));
        final UsageCounter counter = new UsageCounter();
        final Dependencies filtered = new Dependencies(new DependencyMap(), new DependencyMap(), new DependencyMap(),
                handleNotExisting(dependencies, counter),
                handleUndefined(dependencies, counter));
        handleMissing(dependencies, counter, filtered);
        handleDenied(dependencies, counter, filtered);
        collector.printUnusedWarning(counter);
        return new DependencyResult(this, filtered, collector.unusedActions(counter));
    }

    private void handleDenied(Dependencies dependencies, UsageCounter counter, Dependencies filtered) {
        for (final String name : dependencies.denied.getElements()) {
            if (counter.accept(collector.accept(new DependencyEntry(DENIED, className(name))))) {
                filtered.denied.with(name, dependencies.denied);
            }
        }
    }

    private void handleMissing(Dependencies dependencies, UsageCounter counter, Dependencies filtered) {
        for (final String name : dependencies.missing.getElements()) {
            if (counter.accept(collector.accept(new DependencyEntry(MISSING, className(name))))) {
                filtered.missing.with(name, dependencies.missing);
            }
        }
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
