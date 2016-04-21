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

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class MatchCollector extends BaseCollector<Match, Ignore, MatchCollector> {
    public MatchCollector() {
        super(true);
    }

    @Override
    public MatchCollector config(final CollectorConfig<Ignore>... configs) {
        return new MatchCollector() {
            @Override
            public boolean accept(Issue<Match> issue) {
                return accept(issue, MatchCollector.this, configs);
            }

            @Override
            public String toString() {
                return MatchCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    protected boolean doAccept(Match issue, Ignore action) {
        for (final Iterator<Mark> it = issue.iterator(); it.hasNext(); ) {
            final Mark mark = it.next();
            if (!action.accept(new NamedLocation(mark.getSourceCodeSlice(), PmdUtils.className(mark), "", false))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean doAccept(Match issue) {
        return true;
    }

    @Override
    public List<Ignore> unused(RejectCounter counter) {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "";
    }

}
