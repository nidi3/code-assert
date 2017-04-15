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
package guru.nidi.codeassert.model;


import guru.nidi.codeassert.model.p1.ExampleInnerAnnotation;
import guru.nidi.codeassert.model.p2.ExampleEnum;
import guru.nidi.codeassert.model.p3.ExampleSecondEnum;

import java.lang.annotation.Retention;

import static guru.nidi.codeassert.model.p3.ExampleSecondEnum.NO_DEPENDENCIES_ON_ME;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ExampleAnnotation {

    Class<?> c1() default Object.class;

    Class<?> c2() default Object.class;

    ExampleInnerAnnotation c3();

    ExampleEnum c4();

    ExampleSecondEnum c5() default NO_DEPENDENCIES_ON_ME;

}
