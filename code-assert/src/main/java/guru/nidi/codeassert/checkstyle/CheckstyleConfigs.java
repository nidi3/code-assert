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
package guru.nidi.codeassert.checkstyle;

import guru.nidi.codeassert.config.*;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;

public final class CheckstyleConfigs {
    private static final String VARIABLE_PATTERN = "^[a-z][a-zA-Z0-9]*$";

    private CheckstyleConfigs() {
    }

    public static StyleChecks.Google adjustedGoogleStyleChecks() {
        return StyleChecks.google()
                .maxLineLen(120).indentBasic(4).indentCase(4)
                .paramName(VARIABLE_PATTERN)
                .catchParamName(VARIABLE_PATTERN)
                .localVarName(VARIABLE_PATTERN)
                .memberName(VARIABLE_PATTERN)
                .methodName(VARIABLE_PATTERN)
                .emptyLineSeparatorTokens(IMPORT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF,
                        STATIC_INIT, INSTANCE_INIT, METHOD_DEF, CTOR_DEF, VARIABLE_DEF);
    }

    public static CollectorTemplate<Ignore> minimalCheckstyleIgnore() {
        return CollectorTemplate.of(Ignore.class)
                .because("I don't agree", In.everywhere()
                        .ignore("import.avoidStar", "custom.import.order.nonGroup.expected", "custom.import.order.lex",
                                "javadoc.packageInfo", "javadoc.missing",
                                "multiple.variable.declarations.comma", "final.parameter",
                                "design.forExtension", "hidden.field", "inline.conditional.avoid", "magic.number"));
    }

    public static StyleChecks.Sun adjustedSunStyleChecks() {
        return StyleChecks.sun().maxLineLen(120).allowDefaultAccessMembers(true);
    }

}
