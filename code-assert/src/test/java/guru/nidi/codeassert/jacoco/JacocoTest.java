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
package guru.nidi.codeassert.jacoco;

import guru.nidi.codeassert.AnalyzerException;
import guru.nidi.codeassert.config.For;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacocoTest {

    @Test
    void noData() {
        assertThrows(AnalyzerException.class, new Executable() {
            public void execute() throws Throwable {
                new JacocoAnalyzer(new File("target"), new CoverageCollector());
            }
        });
    }

    @Test
    void wrongData() {
        assertThrows(AnalyzerException.class, new Executable() {
            public void execute() throws Throwable {
                new JacocoAnalyzer(new File("target/text-classes/test.zip"), new CoverageCollector()).analyze();
            }
        });
    }

    @Test
    void global() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.global().setMinima(70, 70, 70)));
        assertOutput(result,
                "<global>                                                     59 / 70      61 / 70      50 / 70     ");
    }

    @Test
    void toManyValues() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() throws Throwable {
                new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                        .just(For.global().setMinima(70, 70, 70, 70)));
            }
        });
    }

    @Test
    void toLessValues() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allInPackage("org.springframework.handler").setMinima(40, 50)));
        assertOutput(result, ""
                + "org.springframework.handler.InputPersister                   24 / 40      50 / 50      40 / na     \n"
                + "org.springframework.handler.MailReceiver                      7 / 40      40 / 50      33 / na     ");
    }

    @Test
    void packages() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allPackages().setMinima(40, 50, 40)));
        assertOutput(result, ""
                + "org.springframework.config                                   81 / 40      87 / 50      19 / 40     \n"
                + "org.springframework.data.neo4j.repository.query              70 / 40      78 / 50      39 / 40     \n"
                + "org.springframework.event                                    80 / 40      80 / 50      24 / 40     \n"
                + "org.springframework.persist                                  19 / 40      19 / 50      52 / 40     ");
    }

    @Test
    void packageClasses() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allInPackage("org.springframework.handler").setMinima(40, 50, 40)));
        assertOutput(result, ""
                + "org.springframework.handler.InputPersister                   24 / 40      50 / 50      40 / 40     \n"
                + "org.springframework.handler.MailReceiver                      7 / 40      40 / 50      33 / 40     ");
    }

    @Test
    void classes() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allClasses().setMinima(30, 30, 30)));
        assertOutput(result, ""
                + "org.springframework.config.MailSparkConverter                 7 / 30      50 / 30      50 / 30     \n"
                + "org.springframework.handler.InputPersister                   24 / 30      50 / 30      40 / 30     \n"
                + "org.springframework.handler.MailReceiver                      7 / 30      40 / 30      33 / 30     \n"
                + "org.springframework.parsing.SingleLineValueRetriever.Boundar  0 / 30       0 / 30       0 / 30     \n"
                + "org.springframework.parsing.strategy.algo.BodyParser          0 / 30       0 / 30       0 / 30     \n"
                + "org.springframework.persist.JsonRepositoryImpl                8 / 30       6 / 30       7 / 30     \n"
                + "org.springframework.persist.TransactionalEventBus            19 / 30      12 / 30      13 / 30     ");
    }

    @Test
    void classesWithWildcard() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allClasses().setMinima(30, 30, 30))
                .just(For.loc("org.springframework.p*").setNoMinima()));
        assertOutput(result, ""
                + "org.springframework.config.MailSparkConverter                 7 / 30      50 / 30      50 / 30     \n"
                + "org.springframework.handler.InputPersister                   24 / 30      50 / 30      40 / 30     \n"
                + "org.springframework.handler.MailReceiver                      7 / 30      40 / 30      33 / 30     ");
    }

    @Test
    void explicit() {
        final JacocoResult result = analyze(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.loc("org.springframework.handler.InputPersister").setMinima(30, 30, 30))
                .just(For.thePackage("org.springframework.event").setMinima(30, 30, 30)));
        assertOutput(result, ""
                + "org.springframework.event                                    80 / 30      80 / 30      24 / 30     \n"
                + "org.springframework.handler.InputPersister                   24 / 30      50 / 30      40 / 30     ");
    }

    private JacocoResult analyze(CoverageCollector collector) {
        return new JacocoAnalyzer(new File("target/test-classes"), collector).analyze();
    }

    private void assertOutput(JacocoResult result, String expected) {
        final StringDescription sd = new StringDescription();
        new CoverageMatcher().describeMismatchSafely(result, sd);
        assertThat(sd.toString(), equalTo("Found unsatisfied test coverage requirements:\n"
                + "Analyzed coverage types:                                    INSTRUCTION  METHOD       COMPLEXITY   \n"
                + expected));
    }
}
