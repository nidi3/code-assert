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
package guru.nidi.codeassert;

import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.dependency.*;
import guru.nidi.codeassert.junit.CodeAssertCoreJunit5Test;
import guru.nidi.codeassert.model.Model;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Field;
import java.util.Locale;

import static guru.nidi.codeassert.dependency.DependencyRules.denyAll;

public class EatYourOwnDogfoodTest extends CodeAssertCoreJunit5Test {
    static {
        if (System.getenv("WINDOWS_NEWLINES") != null) {
            windowsNewlines();
        }
    }

    public static void windowsNewlines() {
        try {
            final Field f = System.class.getDeclaredField("lineSeparator");
            f.setAccessible(true);
            f.set(null, "\r\n");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @BeforeAll
    static void init() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Override
    protected DependencyResult analyzeDependencies() {
        class GuruNidiCodeassert extends DependencyRuler {
            DependencyRule graphvizLib = denyRule("guru.nidi.graphviz").andAllSub();
            DependencyRule config, dependency, io, model, util, junit, jacoco;

            @Override
            public void defineRules() {
                base().mayBeUsedBy(all());
                config.mayBeUsedBy(all());
                util.mayBeUsedBy(all());
                dependency.mayUse(model);
                junit.mayUse(model, dependency, jacoco);
                io.mayUse(jacoco, model, graphvizLib);
            }
        }

        final DependencyRules rules = denyAll()
                .withExternals("java.*", "org.*", "kotlin.*")
                .withRelativeRules(new GuruNidiCodeassert());
        return new DependencyAnalyzer(AnalyzerConfig.maven().main()).rules(rules).analyze();
    }

    @Override
    protected Model createModel() {
        return Model.from(AnalyzerConfig.maven().main().getClasses()).read();
    }
}
