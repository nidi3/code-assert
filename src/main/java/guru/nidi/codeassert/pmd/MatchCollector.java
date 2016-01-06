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
package guru.nidi.codeassert.pmd;

import guru.nidi.codeassert.util.LocationMatcher;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 */
public class MatchCollector {
    public MatchCollector ignore(final Class<?>... classes) {
        final String[] cs = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            cs[i] = classes[i].getName();
        }
        return ignore(cs);
    }

    public MatchCollector ignore(final String... locations) {
        return new MatchCollector() {
            private final LocationMatcher matcher = new LocationMatcher(Arrays.asList(locations), Collections.<String>emptyList());

            @Override
            public boolean accept(Match match) {
                return MatchCollector.this.accept(match) && !matchAll(match);
            }

            private boolean matchAll(Match match) {
                for (final Iterator<Mark> it = match.iterator(); it.hasNext(); ) {
                    final Mark mark = it.next();
                    if (!matcher.matches("", className(mark.getFilename()), "")) {
                        return false;
                    }
                }
                return true;
            }

            private String className(String filename) {
                final int pos = filename.lastIndexOf('/');
                return filename.substring(pos + 1, filename.length() - 5);
            }
        };
    }

    public boolean accept(Match match) {
        return true;
    }
}
