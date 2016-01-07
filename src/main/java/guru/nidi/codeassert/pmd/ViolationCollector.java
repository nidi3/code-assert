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

import guru.nidi.codeassert.util.BaseIgnores;
import guru.nidi.codeassert.util.IgnoreSource;
import guru.nidi.codeassert.util.LocationMatcher;
import guru.nidi.codeassert.util.Reason;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;

/**
 *
 */
public class ViolationCollector implements IgnoreSource<ViolationCollector> {
    public static ViolationCollector simple(final RulePriority minPriority) {
        return new ViolationCollector() {
            @Override
            public boolean accept(RuleViolation violation) {
                return (minPriority == null || violation.getRule().getPriority().getPriority() <= minPriority.getPriority());
            }
        };
    }

    public Reason<ViolationCollector> because(String reason) {
        return new Reason<>(this, reason);
    }

    public Ignores ignore(String... names) {
        return new Ignores(names);
    }

    public Ignores ignoreAll() {
        return new Ignores(new String[0]);
    }

    public boolean accept(RuleViolation violation) {
        return true;
    }

    public class Ignores extends BaseIgnores<ViolationCollector> {
        protected Ignores(String[] ignores) {
            super(ignores);
        }

        public ViolationCollector in(final LocationMatcher matcher) {
            return new ViolationCollector() {
                @Override
                public boolean accept(RuleViolation violation) {
                    return ViolationCollector.this.accept(violation) &&
                            !matcher.matches(violation.getRule().getName(), PmdUtils.className(violation), violation.getMethodName());
                }
            };
        }
    }
}
