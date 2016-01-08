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

import guru.nidi.codeassert.config.Action;
import guru.nidi.codeassert.config.BaseCollector;
import guru.nidi.codeassert.config.CollectorConfig;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

import java.util.Iterator;

/**
 *
 */
public class MatchCollector extends BaseCollector<Match, MatchCollector> {
    @Override
    public MatchCollector config(final CollectorConfig... configs) {
        return new MatchCollector() {
            @Override
            public boolean accept(Match issue) {
                return accept(issue, MatchCollector.this, configs);
            }
        };
    }

    @Override
    protected boolean matches(Action action, Match issue) {
        for (final Iterator<Mark> it = issue.iterator(); it.hasNext(); ) {
            final Mark mark = it.next();
            if (!action.matches("", className(mark.getFilename()), "")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean accept(Match issue) {
        return true;
    }

    private String className(String filename) {
        final int pos = filename.lastIndexOf('/');
        return filename.substring(pos + 1, filename.length() - 5);
    }
}
