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
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

import java.util.List;

public class PmdViolationCollector extends BaseCollector<RuleViolation, Ignore, PmdViolationCollector> {
    private final RulePriority minPriority;

    public PmdViolationCollector() {
        this(null);
    }

    private PmdViolationCollector(RulePriority minPriority) {
        this.minPriority = minPriority;
    }

    public PmdViolationCollector minPriority(RulePriority minPriority) {
        return new PmdViolationCollector(minPriority);
    }

    @Override
    public PmdViolationCollector config(final CollectorConfig<Ignore>... configs) {
        return new PmdViolationCollector(minPriority) {
            @Override
            public ActionResult accept(RuleViolation issue) {
                return accept(issue, PmdViolationCollector.this, configs);
            }

            @Override
            public String toString() {
                return PmdViolationCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public ActionResult accept(RuleViolation issue) {
        final int issuePrio = issue.getRule().getPriority().getPriority();
        return new ActionResult(minPriority == null || issuePrio <= minPriority.getPriority(), null, 1);
    }

    @Override
    protected ActionResult doAccept(RuleViolation issue, Ignore action) {
        final Language language = Language.byFilename(issue.getFilename());
        return action.accept(new NamedLocation(
                issue.getRule().getName(), language, PmdUtils.className(issue), issue.getMethodName(), true));
    }

    @Override
    public List<Ignore> unused(UsageCounter counter) {
        return unusedNullAction(counter, minPriority != null);
    }

    @Override
    public String toString() {
        return (minPriority == null ? "" : ("Priority >= " + minPriority + " "));
    }
}
