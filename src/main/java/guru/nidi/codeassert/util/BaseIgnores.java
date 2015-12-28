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
package guru.nidi.codeassert.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseIgnores<T extends IgnoreSource<T>> {
    private final List<String> ignores;

    protected BaseIgnores(String[] ignores) {
        this.ignores = Arrays.asList(ignores);
    }

    public T in(final Class<?>... locs) {
        final List<String> ls = new ArrayList<>();
        for (final Class<?> loc : locs) {
            ls.add(loc.getName());
        }
        return in(new LocationMatcher(ls, ignores));
    }

    public T in(final String... locs) {
        return in(new LocationMatcher(Arrays.asList(locs), ignores));
    }

    public abstract T in(LocationMatcher matcher);

    public T generally() {
        return in(new String[0]);
    }

    public BaseIgnores<T> ignore(String... types) {
        return generally().ignore(types);
    }
}
