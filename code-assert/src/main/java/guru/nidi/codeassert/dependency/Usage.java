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
package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.config.LocationMatcher;

import java.util.HashSet;
import java.util.Set;

class Usage {
    final Set<LocationMatcher> must = new HashSet<>();
    final Set<LocationMatcher> may = new HashSet<>();
    final Set<LocationMatcher> mustNot = new HashSet<>();

    public Usage copy() {
        final Usage u = new Usage();
        u.must.addAll(must);
        u.may.addAll(may);
        u.mustNot.addAll(mustNot);
        return u;
    }

    public void must(CodeElement... rules) {
        for (final CodeElement rule : rules) {
            must.add(rule.pattern);
        }
    }

    public void may(CodeElement... rules) {
        for (final CodeElement rule : rules) {
            may.add(rule.pattern);
        }
    }

    public void mustNot(CodeElement... rules) {
        for (final CodeElement rule : rules) {
            mustNot.add(rule.pattern);
        }
    }

    public boolean isEmpty() {
        return must.isEmpty() && may.isEmpty() && mustNot.isEmpty();
    }

    @Override
    public String toString() {
        return "must " + must + ", may " + may + ", must not " + mustNot;
    }
}
