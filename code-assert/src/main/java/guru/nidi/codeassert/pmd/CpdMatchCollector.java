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
package guru.nidi.codeassert.pmd;

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;

public class CpdMatchCollector extends BaseCollector<Match, Ignore, CpdMatchCollector> {
    @Override
    public CpdMatchCollector config(final CollectorConfig<Ignore>... configs) {
        return new CpdMatchCollector() {
            @Override
            public ActionResult accept(Match issue) {
                return accept(issue, CpdMatchCollector.this, configs);
            }

            @Override
            public String toString() {
                return CpdMatchCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    protected ActionResult doAccept(Match issue, Ignore action) {
        ActionResult res = ActionResult.undecided(null);
        final Iterator<Mark> it = issue.iterator();
        while (it.hasNext()) {
            final Mark mark = it.next();
            final Language language = Language.byFilename(mark.getFilename());
            res = res.orMoreQuality(action.accept(
                    new NamedLocation(mark.getSourceCodeSlice(), language, PmdUtils.className(mark), "", false)));
        }
        return res;
    }

    @Override
    public ActionResult accept(Match issue) {
        return ActionResult.accept(null, 1);
    }

    @Override
    public List<Ignore> unused(UsageCounter counter) {
        return emptyList();
    }

    @Override
    public String toString() {
        return "";
    }

}
