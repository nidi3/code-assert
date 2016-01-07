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
import guru.nidi.codeassert.config.Action;
import guru.nidi.codeassert.config.BaseCollector;
import guru.nidi.codeassert.config.CollectorConfig;

/**
 *
 */
public abstract class BugCollector extends BaseCollector<BugInstance, BugCollector> {
    /**
     * @param maxRank     maximum rank for a bug to be collected.
     * @param minPriority minimum priority for a bug to be collected.
     * @return A new BugCollector with the given configuration.
     * @see edu.umd.cs.findbugs.Priorities
     */
    public static BugCollector simple(final Integer maxRank, final Integer minPriority) {
        return new BugCollector() {
            @Override
            public boolean accept(BugInstance issue) {
                return (maxRank == null || issue.getBugRank() <= maxRank) &&
                        (minPriority == null || issue.getPriority() <= minPriority);
            }
        };
    }

    @Override
    public BugCollector config(final CollectorConfig... configs) {
        return new BugCollector() {
            @Override
            public boolean accept(BugInstance issue) {
                return accept(issue, BugCollector.this, configs);
            }
        };
    }

    @Override
    protected boolean matches(Action action, BugInstance issue) {
        final MethodAnnotation method = issue.getPrimaryMethod();
        final String className = issue.getPrimaryClass().getClassName();
        final String methodName = method == null ? null : method.getMethodName();
        return action.matches(issue.getType(), className, methodName);
    }
}
