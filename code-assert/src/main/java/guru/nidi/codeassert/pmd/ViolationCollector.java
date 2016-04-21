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
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

import java.util.List;

/**
 *
 */
public class ViolationCollector extends BaseCollector<RuleViolation, Ignore, ViolationCollector> {
    private final RulePriority minPriority;

    public ViolationCollector() {
        this(null);
    }

    private ViolationCollector(RulePriority minPriority) {
        super(true);
        this.minPriority = minPriority;
    }

    public ViolationCollector minPriority(RulePriority minPriority) {
        return new ViolationCollector(minPriority);
    }

    @Override
    public ViolationCollector config(final CollectorConfig<Ignore>... configs) {
        return new ViolationCollector(minPriority) {
            @Override
            public boolean accept(Issue<RuleViolation> issue) {
                return accept(issue, ViolationCollector.this, configs);
            }

            @Override
            public String toString() {
                return ViolationCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    protected boolean doAccept(RuleViolation issue) {
        return (minPriority == null || issue.getRule().getPriority().getPriority() <= minPriority.getPriority());
    }

    @Override
    protected boolean doAccept(RuleViolation issue, Ignore action) {
        return action.accept(new NamedLocation(issue.getRule().getName(), PmdUtils.className(issue), issue.getMethodName(), true));
    }

    @Override
    public List<Ignore> unused(RejectCounter counter) {
        return unusedNullAction(counter, minPriority != null);
    }

    @Override
    public String toString() {
        return (minPriority == null ? "" : ("Priority >= " + minPriority + " "));
    }
}
