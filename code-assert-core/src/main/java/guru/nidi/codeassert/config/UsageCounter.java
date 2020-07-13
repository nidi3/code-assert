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

import java.util.HashMap;
import java.util.Map;

public class UsageCounter {
    private final Map<Action<?>, Integer> usage = new HashMap<>();

    public int getCount(Action<?> action) {
        final Integer c = usage.get(action);
        return c == null ? 0 : c;
    }

    public boolean accept(ActionResult accept) {
        final Integer count = usage.get(accept.action);
        usage.put(accept.action, (count == null ? 0 : count) + 1);
        return accept.accept;
    }
}
