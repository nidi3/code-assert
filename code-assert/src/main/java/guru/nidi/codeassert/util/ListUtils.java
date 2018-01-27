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
package guru.nidi.codeassert.util;

import java.util.*;

import static java.util.Arrays.asList;

public final class ListUtils {
    private ListUtils() {
    }

    public static String join(String sep, Object[] os) {
        return join(sep, asList(os));
    }

    public static String join(String sep, List<?> os) {
        final StringBuilder res = new StringBuilder();
        for (final Object o : os) {
            res.append(sep).append(o);
        }
        return os.isEmpty() ? "" : res.substring(sep.length());
    }

    public static <T> List<T> concat(List<T> a, List<T> b) {
        final ArrayList<T> res = new ArrayList<>(a);
        res.addAll(b);
        return res;
    }

    public static <T extends Enum<T>> EnumSet<T> concat(EnumSet<T> a, EnumSet<T> b) {
        final EnumSet<T> res = EnumSet.copyOf(a);
        res.addAll(b);
        return res;
    }

}
