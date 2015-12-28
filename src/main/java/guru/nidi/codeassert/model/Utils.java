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
package guru.nidi.codeassert.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Utils {
    private Utils() {
    }

    public static List<String> prepend(String prefix, List<String> ss) {
        if (ss.isEmpty()) {
            return Collections.singletonList(prefix);
        }
        final List<String> res = new ArrayList<>();
        for (String s : ss) {
            res.add(prefix + s);
        }
        return res;
    }

    public static List<String> merge(List<String> ss1, List<String> ss2) {
        final List<String> res = new ArrayList<>();
        res.addAll(ss1);
        res.addAll(ss2);
        return res;
    }

    public static String join(List<String> ss) {
        final StringBuilder res = new StringBuilder();
        for (final String s : ss) {
            res.append(",").append(s);
        }
        return ss.isEmpty() ? "" : res.substring(1);
    }
}
