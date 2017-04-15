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
package guru.nidi.codeassert.dependency;

import guru.nidi.codeassert.model.UsingElement;

public class AmbiguousRuleException extends RuntimeException {
    private final DependencyRule rule;
    private final UsingElement<?> from;
    private final UsingElement<?> to;

    public AmbiguousRuleException(DependencyRule rule, UsingElement<?> from, UsingElement<?> to) {
        this.rule = rule;
        this.from = from;
        this.to = to;
    }

    public DependencyRule getRule() {
        return rule;
    }

    public UsingElement<?> getFrom() {
        return from;
    }

    public UsingElement<?> getTo() {
        return to;
    }

    @Override
    public String getMessage() {
        return rule + " is ambiguous for dependency " + from + " -> " + to;
    }
}
