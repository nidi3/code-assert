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
package guru.nidi.codeassert.model;

import guru.nidi.codeassert.config.LocationMatcher;

import java.util.Collection;
import java.util.List;

public abstract class UsingElement<T> {
    public abstract T self();

    public abstract String getName();

    public abstract String getPackageName();

    public abstract Collection<String> usedVia(UsingElement<T> other);

    public abstract boolean isMatchedBy(LocationMatcher matcher);

    public abstract Collection<T> uses();

    public boolean uses(T elem) {
        return uses().contains(elem);
    }

    public int mostSpecificMatch(Collection<LocationMatcher> matchers) {
        int s = 0;
        for (final LocationMatcher matcher : matchers) {
            if (isMatchedBy(matcher) && matcher.specificity() > s) {
                s = matcher.specificity();
            }
        }
        return s;
    }

    public boolean matchesAny(List<? extends UsingElementMatcher> matchers) {
        for (final UsingElementMatcher matcher : matchers) {
            if (matcher.matches(this)) {
                return true;
            }
        }
        return false;
    }

}
