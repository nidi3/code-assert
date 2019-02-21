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
package guru.nidi.codeassert.findbugs;

import guru.nidi.codeassert.config.*;

import static guru.nidi.codeassert.config.Language.KOTLIN;

public final class FindBugsConfigs {
    private FindBugsConfigs() {
    }

    public static CollectorTemplate<Ignore> minimalFindBugsIgnore() {
        return CollectorTemplate.forA(BugCollector.class)
                .because("modern compilers are clever", In.everywhere().ignore(
                        "SBSC_USE_STRINGBUFFER_CONCATENATION"))
                .because("it's compiler generated code", In.languages(KOTLIN).ignore(
                        "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "BC_BAD_CAST_TO_ABSTRACT_COLLECTION"))
                .because("it's compiler generated code, but why?", In.languages(KOTLIN).ignore(
                        "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE"))
                .because("findbugs seems to be cleverer than kotlin compiler", In.languages(KOTLIN).ignore(
                        "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"))
                .because("inline methods seem to cause this", In.languages(KOTLIN).ignore(
                        "UPM_UNCALLED_PRIVATE_METHOD"));
    }

    public static CollectorTemplate<Ignore> dependencyTestIgnore(Class<?> dependencyTest) {
        return CollectorTemplate.of(Ignore.class).just(In.clazz(dependencyTest)
                .ignore("NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD"));
    }

}
