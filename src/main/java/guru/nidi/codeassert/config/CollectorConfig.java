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

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public final class CollectorConfig {
    public final String reason;
    public final List<Action> actions;

    private CollectorConfig(String reason, List<Action> actions) {
        this.reason = reason;
        this.actions = actions;
    }

    public static CollectorConfig because(String reason, Action... actions) {
        return new CollectorConfig(reason, Arrays.asList(actions));
    }

    public static CollectorConfig just(Action... actions) {
        return because(null, actions);
    }
}