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

public final class PmdRulesets {
    private PmdRulesets() {
    }

    public static PmdRuleset android() {
        return new PmdRuleset("rulesets/java/android.xml");
    }

    public static PmdRuleset basic() {
        return new PmdRuleset("rulesets/java/basic.xml");
    }

    public static PmdRuleset braces() {
        return new PmdRuleset("rulesets/java/braces.xml");
    }

    public static PmdRuleset cloning() {
        return new PmdRuleset("rulesets/java/clone.xml");
    }

    public static Codesize codesize() {
        return new Codesize();
    }

    public static Comments comments() {
        return new Comments();
    }

    public static PmdRuleset controversial() {
        return new PmdRuleset("rulesets/java/controversial.xml");
    }

    public static PmdRuleset coupling() {
        return new PmdRuleset("rulesets/java/coupling.xml");
    }

    public static PmdRuleset design() {
        return new PmdRuleset("rulesets/java/design.xml");
    }

    public static Empty empty() {
        return new Empty();
    }

    public static PmdRuleset finalizers() {
        return new PmdRuleset("rulesets/java/finalizers.xml");
    }

    public static PmdRuleset imports() {
        return new PmdRuleset("rulesets/java/imports.xml");
    }

    public static PmdRuleset j2ee() {
        return new PmdRuleset("rulesets/java/j2ee.xml");
    }

    public static PmdRuleset javabeans() {
        return new PmdRuleset("rulesets/java/javabeans.xml");
    }

    public static PmdRuleset junit() {
        return new PmdRuleset("rulesets/java/junit.xml");
    }

    public static Naming naming() {
        return new Naming();
    }

    public static PmdRuleset optimizations() {
        return new PmdRuleset("rulesets/java/optimizations.xml");
    }

    public static PmdRuleset exceptions() {
        return new PmdRuleset("rulesets/java/strictexception.xml");
    }

    public static PmdRuleset strings() {
        return new PmdRuleset("rulesets/java/strings.xml");
    }

    public static PmdRuleset sunSecure() {
        return new PmdRuleset("rulesets/java/sunsecure.xml");
    }

    public static PmdRuleset typeResolution() {
        return new PmdRuleset("rulesets/java/typeresolution.xml");
    }

    public static PmdRuleset unnecessary() {
        return new PmdRuleset("rulesets/java/unnecessary.xml");
    }

    public static PmdRuleset unused() {
        return new PmdRuleset("rulesets/java/unusedcode.xml");
    }

    public static class Codesize extends PmdRuleset {
        @PropertyField(rule = "ExcessiveMethodLength", property = "minimum")
        private Double methodLength;
        @PropertyField(rule = "TooManyMethods", property = "maxmethods")
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

    public static class Empty extends PmdRuleset {
        @PropertyField(rule = "EmptyCatchBlock", property = "allowCommentedBlocks")
        private Boolean allowCommented;

        public Empty() {
            super("rulesets/java/empty.xml");
        }

        public Empty allowCommentedEmptyCatch(boolean allow) {
            this.allowCommented = allow;
            return this;
        }
    }

    public static class Comments extends PmdRuleset {
        public enum Requirement {
            Required, Ignored, Unwanted
        }

        @PropertyField(rule = "CommentRequired", property = "headerCommentRequirement")
        private Requirement header;
        @PropertyField(rule = "CommentRequired", property = "fieldCommentRequirement")
        private Requirement field;
        @PropertyField(rule = "CommentRequired", property = "publicMethodCommentRequirement")
        private Requirement publicMethod;
        @PropertyField(rule = "CommentRequired", property = "protectedMethodCommentRequirement")
        private Requirement protectedMethod;
        @PropertyField(rule = "CommentRequired", property = "enumCommentRequirement")
        private Requirement enums;
        @PropertyField(rule = "CommentRequired", property = "serialVersionUIDCommentRequired")
        private Requirement serialVersionUID;
        @PropertyField(rule = "CommentSize", property = "maxLines")
        private Integer maxLines;
        @PropertyField(rule = "CommentSize", property = "maxLineLength")
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

        public Comments maxLineLen(int maxLen) {
            maxLineLength = maxLen;
            return this;
        }
    }

    public static class Naming extends PmdRuleset {
        @PropertyField(rule = "ShortVariable", property = "minimum")
        private Integer varMinLen;
        @PropertyField(rule = "LongVariable", property = "minimum")
        private Integer varMaxLen;
        @PropertyField(rule = "ShortMethodName", property = "minimum")
        private Integer methodMinLen;
        @PropertyField(rule = "ShortClassName", property = "minimum")
        private Integer classMinLen;

        public Naming() {
            super("rulesets/java/naming.xml");
        }

        public Naming variableLen(int min, int max) {
            varMinLen = min;
            varMaxLen = max;
            return this;
        }

        public Naming methodLen(int min) {
            methodMinLen = min;
            return this;
        }

        public Naming classLen(int min) {
            classMinLen = min;
            return this;
        }
    }
}
