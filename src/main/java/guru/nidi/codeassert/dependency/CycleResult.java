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
package guru.nidi.codeassert.dependency;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class CycleResult {
    final Set<DependencyMap> cycles;

    public CycleResult() {
        cycles = new HashSet<>();
    }

    public boolean isEmpty() {
        return cycles.isEmpty();
    }

    @SafeVarargs
    public final boolean isEmptyExcept(Set<String>... exceptions) {
        return getCyclesExcept(exceptions).isEmpty();
    }

    @SafeVarargs
    public final Set<DependencyMap> getCyclesExcept(Set<String>... exceptions) {
        final Set<DependencyMap> res = new HashSet<>();
        for (final DependencyMap cycle : cycles) {
            boolean excepted = false;
            for (final Set<String> exception : exceptions) {
                if (exception.containsAll(cycle.getPackages())) {
                    excepted = true;
                    break;
                }
            }
            if (!excepted) {
                res.add(cycle);
            }
        }
        return res;
    }

    public static Set<String> packages(String... packs){
        final HashSet<String> res = new HashSet<>();
        Collections.addAll(res, packs);
        return res;
    }
}
