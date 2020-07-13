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
package guru.nidi.codeassert.model.p4;

import guru.nidi.codeassert.model.p4.p1.Type1;
import guru.nidi.codeassert.model.p4.p10.Type10;
import guru.nidi.codeassert.model.p4.p2.Type2;
import guru.nidi.codeassert.model.p4.p3.Type3;
import guru.nidi.codeassert.model.p4.p4.Type4;
import guru.nidi.codeassert.model.p4.p5.Type5;
import guru.nidi.codeassert.model.p4.p6.Type6;
import guru.nidi.codeassert.model.p4.p7.Type7;
import guru.nidi.codeassert.model.p4.p8.Type8;
import guru.nidi.codeassert.model.p4.p9.Type9;

import java.util.ArrayList;
import java.util.List;

public class GenericParameters<T> {
    private List<Type2> list;
    private List<?> l2 = new ArrayList<Type3>();

    private <E extends Type4 & Type6, X> List<E> method4(Type7 t7, X x) {
        return null;
    }

    private List<? extends Type4> method42() throws Type8 {
        return null;
    }

    private <E extends Type9> void method5(List<? super Type5> param, List<Type1.Type1Sub<String>> sub) throws E {
    }

    private List<Type10[]> method6(List<int[]> a) {
        return null;
    }
}

