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

    public static PmdRuleset bestPractices() {
        return new PmdRuleset("category/java/bestpractices.xml");
    }

    public static CodeStyle codeStyle() {
        return new CodeStyle();
    }

    public static Design design() {
        return new Design();
    }

    public static Documentation documentation() {
        return new Documentation();
    }

    public static ErrorProne errorProne() {
        return new ErrorProne();
    }

    public static PmdRuleset multithreading() {
        return new PmdRuleset("category/java/multithreading.xml");
    }

    public static PmdRuleset performance() {
        return new PmdRuleset("category/java/performance.xml");
    }

    public static PmdRuleset security() {
        return new PmdRuleset("category/java/security.xml");
    }


    public static class Design extends PmdRuleset {
        @PropertyField(rule = "ExcessiveMethodLength", property = "minimum")
        private Double methodLength;
        @PropertyField(rule = "TooManyMethods", property = "maxmethods")
        private Integer methodCount;

        public Design() {
            super("category/java/design.xml");
        }

        public Design excessiveMethodLength(int limit) {
            methodLength = (double) limit;
            return this;
        }

        public Design tooManyMethods(int limit) {
            methodCount = limit;
            return this;
        }
    }

    public static class ErrorProne extends PmdRuleset {
        @PropertyField(rule = "EmptyCatchBlock", property = "allowCommentedBlocks")
        private Boolean allowCommented;

        public ErrorProne() {
            super("category/java/errorprone.xml");
        }

        public ErrorProne allowCommentedEmptyCatch(boolean allow) {
            this.allowCommented = allow;
            return this;
        }
    }

    public static class Documentation extends PmdRuleset {
        private final Enum[] realRequirements;

        public enum Requirement {
            REQUIRED, IGNORED, UNWANTED
        }

        @PropertyField(rule = "CommentRequired", property = "headerCommentRequirement")
        private Object header;
        @PropertyField(rule = "CommentRequired", property = "fieldCommentRequirement")
        private Object field;
        @PropertyField(rule = "CommentRequired", property = "publicMethodCommentRequirement")
        private Object publicMethod;
        @PropertyField(rule = "CommentRequired", property = "protectedMethodCommentRequirement")
        private Object protectedMethod;
        @PropertyField(rule = "CommentRequired", property = "enumCommentRequirement")
        private Object enums;
        @PropertyField(rule = "CommentRequired", property = "serialVersionUIDCommentRequired")
        private Object serialVersionUID;
        @PropertyField(rule = "CommentSize", property = "maxLines")
        private Integer maxLines;
        @PropertyField(rule = "CommentSize", property = "maxLineLength")
        private Integer maxLineLength;

        public Documentation() {
            super("category/java/documentation.xml");
            try {
                final Class<?> reqClass = Class.forName("net.sourceforge.pmd.lang.java.rule.documentation.CommentRequiredRule$CommentRequirement");
                realRequirements = (Enum[]) reqClass.getEnumConstants();
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }

        public Documentation requirement(Requirement requirement) {
            header(requirement);
            field(requirement);
            publicMethod(requirement);
            protectedMethod(requirement);
            enums(requirement);
            serialVersionUID(requirement);
            return this;
        }

        private Object transform(Requirement req) {
            for (final Enum real : realRequirements) {
                if (real.name().equals(req.name())) {
                    return real;
                }
            }
            return null;
        }

        public Documentation header(Requirement requirement) {
            header = transform(requirement);
            return this;
        }

        public Documentation field(Requirement requirement) {
            field = transform(requirement);
            return this;
        }

        public Documentation publicMethod(Requirement requirement) {
            publicMethod = transform(requirement);
            return this;
        }

        public Documentation protectedMethod(Requirement requirement) {
            protectedMethod = transform(requirement);
            return this;
        }

        public Documentation enums(Requirement requirement) {
            enums = transform(requirement);
            return this;
        }

        public Documentation serialVersionUID(Requirement requirement) {
            serialVersionUID = transform(requirement);
            return this;
        }

        public Documentation maxLines(int lines) {
            maxLines = lines;
            return this;
        }

        public Documentation maxLineLen(int maxLen) {
            maxLineLength = maxLen;
            return this;
        }
    }

    public static class CodeStyle extends PmdRuleset {
        @PropertyField(rule = "ShortVariable", property = "minimum")
        private Integer varMinLen;
        @PropertyField(rule = "LongVariable", property = "minimum")
        private Integer varMaxLen;
        @PropertyField(rule = "ShortMethodName", property = "minimum")
        private Integer methodMinLen;
        @PropertyField(rule = "ShortClassName", property = "minimum")
        private Integer classMinLen;

        public CodeStyle() {
            super("category/java/codestyle.xml");
        }

        public CodeStyle variableLen(int min, int max) {
            varMinLen = min;
            varMaxLen = max;
            return this;
        }

        public CodeStyle methodLen(int min) {
            methodMinLen = min;
            return this;
        }

        public CodeStyle classLen(int min) {
            classMinLen = min;
            return this;
        }
    }
}
