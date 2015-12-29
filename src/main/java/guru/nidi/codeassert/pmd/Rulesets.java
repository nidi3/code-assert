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

    public static Ruleset android() {
        return new Ruleset("rulesets/java/android.xml");
    }

    public static Ruleset basic() {
        return new Ruleset("rulesets/java/basic.xml");
    }

    public static Ruleset braces() {
        return new Ruleset("rulesets/java/braces.xml");
    }

    public static Ruleset cloning() {
        return new Ruleset("rulesets/java/clone.xml");
    }

    public static Codesize codesize() {
        return new Codesize();
    }

    public static Comments comments() {
        return new Comments();
    }

    public static Ruleset controversial() {
        return new Ruleset("rulesets/java/controversial.xml");
    }

    public static Ruleset design() {
        return new Ruleset("rulesets/java/design.xml");
    }

    public static Ruleset optimizations() {
        return new Ruleset("rulesets/java/optimizations.xml");
    }


    public static Empty empty() {
        return new Empty();
    }

    public static class Codesize extends Ruleset {
        @PropertyField(rule = "ExcessiveMethodLength", value = "minimum")
        private Double methodLength;
        @PropertyField(rule = "TooManyMethods", value = "maxmethods")
        private Integer methodCount;

        public Codesize() {
            super("rulesets/java/codesize.xml");
        }

        public Codesize excessiveMethodLength(int limit) {
            methodLength = (double) limit;
            return this;
        }

        public Codesize tooManyMethods(int limit) {
            methodCount = limit;
            return this;
        }
    }

    public static class Empty extends Ruleset {
        public EmptyCatchBlock emptyCatchBlock = new EmptyCatchBlock(this);

        public Empty() {
            super("rulesets/java/empty.xml");
        }

        public static class EmptyCatchBlock extends RuleDescriptor<Empty> {
            @PropertyField("allowCommentedBlocks")
            private Boolean allowCommented;

            public EmptyCatchBlock(Empty ruleset) {
                super(ruleset);
            }

            public Empty allowCommented(boolean allow) {
                this.allowCommented = allow;
                return ruleset;
            }
        }
    }

    public static class Comments extends Ruleset {
        enum Requirement {
            Required, Ignored, Unwanted
        }

        @PropertyField(rule = "CommentRequired", value = "headerCommentRequirement")
        private Requirement header;
        @PropertyField(rule = "CommentRequired", value = "fieldCommentRequirement")
        private Requirement field;
        @PropertyField(rule = "CommentRequired", value = "publicMethodCommentRequirement")
        private Requirement publicMethod;
        @PropertyField(rule = "CommentRequired", value = "protectedMethodCommentRequirement")
        private Requirement protectedMethod;
        @PropertyField(rule = "CommentRequired", value = "enumCommentRequirement")
        private Requirement enums;
        @PropertyField(rule = "CommentRequired", value = "serialVersionUIDCommentRequired")
        private Requirement serialVersionUID;
        @PropertyField(rule = "CommentSize", value = "maxLines")
        private Integer maxLines;
        @PropertyField(rule = "CommentSize", value = "maxLineLength")
        private Integer maxLineLength;

        public Comments() {
            super("rulesets/java/comments.xml");
        }

        public Comments requirement(Requirement requirement) {
            header(requirement);
            field(requirement);
            publicMethod(requirement);
            protectedMethod(requirement);
            enums(requirement);
            serialVersionUID(requirement);
            return this;
        }

        public Comments header(Requirement requirement) {
            header = requirement;
            return this;
        }

        public Comments field(Requirement requirement) {
            field = requirement;
            return this;
        }

        public Comments publicMethod(Requirement requirement) {
            publicMethod = requirement;
            return this;
        }

        public Comments protectedMethod(Requirement requirement) {
            protectedMethod = requirement;
            return this;
        }

        public Comments enums(Requirement requirement) {
            enums = requirement;
            return this;
        }

        public Comments serialVersionUID(Requirement requirement) {
            serialVersionUID = requirement;
            return this;
        }

        public Comments maxLines(int lines) {
            maxLines = lines;
            return this;
        }

        public Comments maxLineLength(int maxLen) {
            maxLineLength = maxLen;
            return this;
        }
    }
}