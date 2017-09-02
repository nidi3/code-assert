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

public final class In {
    private final List<String> locs;

    private In(List<String> locs) {
        this.locs = locs;
    }

    public static In classes(final Class<?>... classes) {
        final List<String> ls = new ArrayList<>();
        for (final Class<?> loc : classes) {
            ls.add(loc.getName());
        }
        return new In(ls);
    }

    public static In clazz(final Class<?> clazz) {
        return loc(clazz.getName());
    }

    public static In locs(final String... locs) {
        return new In(Arrays.asList(locs));
    }

    public static In loc(final String loc) {
        return new In(Collections.singletonList(loc));
    }

    public static In everywhere() {
        return locs();
    }

    public Ignore ignore(String... names) {
        return new Ignore(locs, Arrays.asList(names));
    }

    public Ignore ignoreAll() {
        return ignore();
    }
}
