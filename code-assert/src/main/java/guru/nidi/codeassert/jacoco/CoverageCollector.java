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

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;

import java.util.List;

public class CoverageCollector extends BaseCollector<ValuedLocation, Minima, CoverageCollector> {
    final CoverageType[] types;

    public CoverageCollector(CoverageType... types) {
        this.types = types;
    }

    @Override
    public CoverageCollector config(final CollectorConfig<Minima>... configs) {
        for (final CollectorConfig<Minima> config : configs) {
            for (final Minima minima : config.actions) {
                if (minima.getValueCount() > types.length) {
                    throw new IllegalArgumentException("Given " + minima.getValueCount()
                            + " values, but expected only " + types.length);
                }
            }
        }

        return new CoverageCollector(types) {
            @Override
            public ActionResult accept(ValuedLocation issue) {
                return accept(issue, CoverageCollector.this, configs);
            }

            public List<Minima> unused(UsageCounter counter) {
                return unused(counter, CoverageCollector.this, configs);
            }

            @Override
            public String toString() {
                return CoverageCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public ActionResult accept(ValuedLocation issue) {
        return ActionResult.reject(null, 1);
    }

    @Override
    protected ActionResult doAccept(ValuedLocation issue, Minima action) {
        return action.accept(issue);
    }

    @Override
    public List<Minima> unused(UsageCounter counter) {
        return unusedNullAction(counter, true);
    }

    @Override
    public String toString() {
        return "CoverageCollector";
    }
}
