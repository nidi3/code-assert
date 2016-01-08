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
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

/**
 *
 */
public class ViolationCollector extends BaseCollector<RuleViolation, ViolationCollector> {
    private final RulePriority minPriority;

    public ViolationCollector() {
        this(null);
    }

    private ViolationCollector(RulePriority minPriority) {
        this.minPriority = minPriority;
    }

    public ViolationCollector minPriority(RulePriority minPriority) {
        return new ViolationCollector(minPriority);
    }

    @Override
    public boolean accept(RuleViolation issue) {
        return (minPriority == null || issue.getRule().getPriority().getPriority() <= minPriority.getPriority());
    }

    @Override
    public ViolationCollector config(final CollectorConfig... configs) {
        return new ViolationCollector(minPriority) {
            @Override
            public boolean accept(RuleViolation issue) {
                return accept(issue, ViolationCollector.this, configs);
            }
        };
    }

    @Override
    protected boolean matches(Action action, RuleViolation issue) {
        return action.matches(issue.getRule().getName(), PmdUtils.className(issue), issue.getMethodName());
    }
}
