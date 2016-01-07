/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.config;

/**
 *
 */
public abstract class BaseCollector<S, T extends BaseCollector<S, T>> {
    public T because(String reason, Action... actions) {
        return config(CollectorConfig.because(reason, actions));
    }

    public T just(Action... actions) {
        return config(CollectorConfig.just(actions));
    }

    public abstract T config(final CollectorConfig... configs);

    protected boolean accept(S issue, T parent, CollectorConfig... configs) {
        for (final CollectorConfig config : configs) {
            for (final Action action : config.actions) {
                if (matches(action, issue)) {
                    return false;
                }
            }
        }
        return parent.accept(issue);
    }

    protected abstract boolean matches(Action action, S issue);

    public abstract boolean accept(S issue);
}