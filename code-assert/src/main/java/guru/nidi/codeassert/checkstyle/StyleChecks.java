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

import java.util.*;

import static com.puppycrawl.tools.checkstyle.api.TokenTypes.*;
import static java.util.Arrays.asList;

public class StyleChecks {
    private static final String MAX_LINE_LEN = "maxLineLen";

    final String location;
    final Map<String, Object> params = new HashMap<>();

    private StyleChecks(String location, Object... defaults) {
        this.location = location;
        for (int i = 0; i < defaults.length; i += 2) {
            params.put((String) defaults[i], defaults[i + 1]);
        }
    }

    public static StyleChecks from(String fileOrClasspath) {
        return new StyleChecks(fileOrClasspath);
    }

    public static Google google() {
        return new Google();
    }

    public static Sun sun() {
        return new Sun();
    }

    protected <T extends StyleChecks> T withParam(String name, Object value) {
        params.put(name, value);
        return (T) this;
    }

    public static final class Google extends StyleChecks {
        protected Google() {
            super("/guru/nidi/codeassert/checkstyle/google_checks.xml",
                    MAX_LINE_LEN, 100,
                    "indent-basicOffset", 2, "indent-case", 2, "indent-arrayInit", 2, "indent-throws", 4,
                    "indent-lineWrapping", 4,
                    "parameterName", "^[a-z][a-z0-9][a-zA-Z0-9]*$",
                    "catchParameterName", "^[a-z][a-z0-9][a-zA-Z0-9]*$",
                    "localVariableName", "^[a-z][a-z0-9][a-zA-Z0-9]*$",
                    "emptyLine-tokens", asList(PACKAGE_DEF, IMPORT, CLASS_DEF, INTERFACE_DEF, ENUM_DEF,
                            STATIC_INIT, INSTANCE_INIT, METHOD_DEF, CTOR_DEF, VARIABLE_DEF));
        }

        public Google maxLineLen(int maxLineLen) {
            return withParam(MAX_LINE_LEN, maxLineLen);
        }

        public Google indentBasic(int indentBasic) {
            return withParam("indent-basicOffset", indentBasic);
        }

        public Google indentCase(int indentCase) {
            return withParam("indent-case", indentCase);
        }

        public Google indentArrayInit(int indentArrayInit) {
            return withParam("indent-arrayInit", indentArrayInit);
        }

        public Google indentThrows(int indentThrows) {
            return withParam("indent-throws", indentThrows);
        }

        public Google indentLineWrapping(int indentLineWrapping) {
            return withParam("indent-lineWrapping", indentLineWrapping);
        }

        public Google paramName(String pattern) {
            return withParam("parameterName", pattern);
        }

        public Google catchParamName(String pattern) {
            return withParam("catchParameterName", pattern);
        }

        public Google localVarName(String pattern) {
            return withParam("localVariableName", pattern);
        }

        public Google emptyLineSeparatorTokens(int... tokens) {
            final List<Integer> ts = new ArrayList<>();
            for (final int token : tokens) {
                ts.add(token);
            }
            return withParam("emptyLine-tokens", ts);
        }

    }

    public static final class Sun extends StyleChecks {
        protected Sun() {
            super("/guru/nidi/codeassert/checkstyle/sun_checks.xml",
                    MAX_LINE_LEN, 80, "allowDefaultAccessMembers", false);
        }

        public Sun maxLineLen(int maxLineLen) {
            return withParam(MAX_LINE_LEN, maxLineLen);
        }

        public Sun allowDefaultAccessMembers(boolean allow) {
            return withParam("allowDefaultAccessMembers", allow);
        }
    }

}
