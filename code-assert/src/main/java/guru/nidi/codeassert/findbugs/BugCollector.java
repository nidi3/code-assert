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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;

import java.util.List;

public class BugCollector extends BaseCollector<BugInstance, Ignore, BugCollector> {
    private final Integer maxRank;
    private final Integer minPriority;

    public BugCollector() {
        this(null, null);
    }

    private BugCollector(Integer maxRank, Integer minPriority) {
        this.maxRank = maxRank;
        this.minPriority = minPriority;
    }

    public BugCollector maxRank(int maxRank) {
        return new BugCollector(maxRank, minPriority);
    }

    /**
     * @param minPriority minimum priority for a bug to be collected.
     * @return A new BugCollector with the given configuration.
     * @see edu.umd.cs.findbugs.Priorities
     */
    public BugCollector minPriority(int minPriority) {
        return new BugCollector(maxRank, minPriority);
    }

    @Override
    public BugCollector config(final CollectorConfig<Ignore>... configs) {
        return new BugCollector(maxRank, minPriority) {
            @Override
            public ActionResult accept(BugInstance issue) {
                return accept(issue, BugCollector.this, configs);
            }

            public List<Ignore> unused(UsageCounter counter) {
                return unused(counter, BugCollector.this, configs);
            }

            @Override
            public String toString() {
                return BugCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public ActionResult accept(BugInstance issue) {
        return new ActionResult((maxRank == null || issue.getBugRank() <= maxRank)
                && (minPriority == null || issue.getPriority() <= minPriority), null, 1
        );
    }

    @Override
    protected ActionResult doAccept(BugInstance issue, Ignore action) {
        final MethodAnnotation method = issue.getPrimaryMethod();
        final String className = issue.getPrimaryClass().getClassName();
        final String methodName = method == null ? null : method.getMethodName();
        final Language language = Language.byFilename(issue.getPrimaryClass().getSourceFileName());
        return action.accept(new NamedLocation(issue.getType(), language, className, methodName, true));
    }

    @Override
    public List<Ignore> unused(UsageCounter counter) {
        return unusedNullAction(counter, maxRank != null || minPriority != null);
    }

    @Override
    public String toString() {
        return (maxRank == null ? "" : ("Rank <= " + maxRank + " "))
                + (minPriority == null ? "" : ("Priority >= " + minPriority) + " ");
    }

}
