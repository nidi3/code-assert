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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;

import java.util.List;

/**
 *
 */
public class BugCollector extends BaseCollector<BugInstance, BugCollector> {
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
    public BugCollector config(final CollectorConfig... configs) {
        return new BugCollector(maxRank, minPriority) {
            @Override
            public boolean accept(Issue<BugInstance> issue) {
                return accept(issue, BugCollector.this, configs);
            }

            public List<Action> unused(RejectCounter counter) {
                return unused(counter, BugCollector.this, configs);
            }

            @Override
            public String toString() {
                return BugCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public boolean doAccept(BugInstance issue) {
        return (maxRank == null || issue.getBugRank() <= maxRank) &&
                (minPriority == null || issue.getPriority() <= minPriority);
    }

    @Override
    protected boolean doAccept(BugInstance issue, Action action) {
        final MethodAnnotation method = issue.getPrimaryMethod();
        final String className = issue.getPrimaryClass().getClassName();
        final String methodName = method == null ? null : method.getMethodName();
        return action.accept(issue.getType(), className, methodName, true);
    }

    @Override
    public List<Action> unused(RejectCounter counter) {
        return unusedNullAction(counter, maxRank != null || minPriority == null);
    }

    @Override
    public String toString() {
        return (maxRank == null ? "" : ("Rank <= " + maxRank + " ")) +
                (minPriority == null ? "" : ("Priority >= " + minPriority) + " ");
    }
}
