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

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class RejectCounter {
    private final Map<Action, Integer> rejects = new HashMap<>();

    public <S> Issue<S> issue(S rawIssue) {
        return new Issue<>(this, rawIssue);
    }

    public int getCount(Action action) {
        final Integer c = rejects.get(action);
        return c == null ? 0 : c;
    }

    public boolean count(Action action, boolean accept) {
        if (!accept) {
            final Integer count = rejects.get(action);
            rejects.put(action, (count == null ? 0 : count) + 1);
        }
        return accept;
    }
}