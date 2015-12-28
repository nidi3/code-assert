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

/**
 *
 */
public class Rulesets {
    private Rulesets() {
    }

    public static Ruleset basic() {
        return new Ruleset("rulesets/java/basic.xml");
    }

    public static Ruleset braces() {
        return new Ruleset("rulesets/java/braces.xml");
    }

    public static Ruleset design() {
        return new Ruleset("rulesets/java/design.xml");
    }

    public static Ruleset optimizations() {
        return new Ruleset("rulesets/java/optimizations.xml");
    }

    public static Codesize codesize() {
        return new Codesize();
    }

    public static Empty empty() {
        return new Empty();
    }

    public static class Codesize extends Ruleset {
        private Double excessiveMethodLength_minimum;
        private Integer tooManyMethods_maxmethods;

        public Codesize() {
            super("rulesets/java/codesize.xml");
        }

        public Codesize excessiveMethodLength(int limit) {
            excessiveMethodLength_minimum = (double) limit;
            return this;
        }

        public Codesize tooManyMethods(int limit) {
            tooManyMethods_maxmethods = limit;
            return this;
        }
    }

    public static class Empty extends Ruleset {
        public EmptyCatchBlock emptyCatchBlock = new EmptyCatchBlock(this);

        public Empty() {
            super("rulesets/java/empty.xml");
        }

        public static class EmptyCatchBlock extends RuleDescriptor<Empty> {
            private Boolean allowCommentedBlocks;

            public EmptyCatchBlock(Empty ruleset) {
                super(ruleset);
            }

            public Empty allowCommented(boolean allow) {
                this.allowCommentedBlocks = allow;
                return ruleset;
            }
        }
    }
}