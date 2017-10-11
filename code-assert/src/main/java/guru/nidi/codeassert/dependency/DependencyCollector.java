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

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;

import java.util.List;

public class DependencyCollector extends BaseCollector<DependencyEntry, Ignore, DependencyCollector> {
    public static final String MISSING = "MISSING";
    public static final String DENIED = "DENIED";
    public static final String NOT_EXISTING = "NOT_EXISTING";
    public static final String UNDEFINED = "UNDEFINED";
    public static final String CYCLE = "CYCLE";

    @Override
    public DependencyCollector config(final CollectorConfig<Ignore>... configs) {
        return new DependencyCollector() {
            @Override
            public ActionResult accept(DependencyEntry result) {
                return accept(result, DependencyCollector.this, configs);
            }

            public List<Ignore> unused(UsageCounter counter) {
                return unused(counter, DependencyCollector.this, configs);
            }

            @Override
            public String toString() {
                return DependencyCollector.this + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public ActionResult accept(DependencyEntry result) {
        return new ActionResult(true, null, 1);
    }

    @Override
    protected ActionResult doAccept(DependencyEntry result, Ignore action) {
        final NamedLocation loc = new NamedLocation(result.name, null, result.className, "", true);
        return action.accept(loc);
    }

    @Override
    public List<Ignore> unused(UsageCounter counter) {
        return unusedNullAction(counter, false);
    }

    @Override
    public String toString() {
        return "";
    }
}
