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
package guru.nidi.codeassert.config;

import java.util.*;

public final class CollectorTemplate<A extends Action>
        extends BaseCollector<Object, A, CollectorTemplate<A>> implements Iterable<CollectorConfig<A>> {
    private final List<CollectorConfig<A>> configs = new ArrayList<>();

    private CollectorTemplate() {
    }

    public static <A extends Action> CollectorTemplate<A> forA(Class<? extends BaseCollector<?, A, ?>> coll) {
        return new CollectorTemplate<>();
    }

    public static <A extends Action> CollectorTemplate<A> of(Class<A> action) {
        return new CollectorTemplate<>();
    }

    @Override
    public Iterator<CollectorConfig<A>> iterator() {
        return configs.iterator();
    }

    @Override
    protected CollectorTemplate<A> config(CollectorConfig<A>... configs) {
        for (final CollectorConfig<A> config : configs) {
            this.configs.add(config.ignoringUnused());
        }
        return this;
    }

    @Override
    public ActionResult accept(Object issue) {
        throw new UnsupportedOperationException(
                "This is just a template. Apply it to a real Collector using .configs()");
    }

    @Override
    protected ActionResult doAccept(Object issue, A action) {
        throw new UnsupportedOperationException(
                "This is just a template. Apply it to a real Collector using .configs()");
    }

    @Override
    protected List<A> unused(UsageCounter counter) {
        throw new UnsupportedOperationException(
                "This is just a template. Apply it to a real Collector using .configs()");
    }

    @Override
    public String toString() {
        return configs.toString();
    }

}
