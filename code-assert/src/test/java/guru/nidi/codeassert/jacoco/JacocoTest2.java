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
package guru.nidi.codeassert.jacoco;

import guru.nidi.codeassert.config.For;
import org.hamcrest.StringDescription;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static guru.nidi.codeassert.jacoco.CoverageType.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class JacocoTest2 {
    @BeforeClass
    public static void init() throws IOException {
        final File file = new File("target/site/jacoco/jacoco.csv");
        file.getParentFile().mkdirs();
        Files.copy(JacocoTest2.class.getResourceAsStream("/jacoco.csv"), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void global() throws IOException {
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.global().setMinima(70, 70, 70)));
        assertOutput(analyzer.analyze(),
                "<global>                                                     59 / 70      61 / 70      50 / 70     ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void toManyValues() throws IOException {
        new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.global().setMinima(70, 70, 70, 70)));
    }

    @Test
    public void toLessValues() throws IOException {
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allInPackage("org.springframework.handler").setMinima(40, 50)));
        assertOutput(analyzer.analyze(), "" +
                "org.springframework.handler.InputPersister                   24 / 40      50 / 50      40 / na     \n" +
                "org.springframework.handler.MailReceiver                      7 / 40      40 / 50      33 / na     ");
    }

    @Test
    public void packages() throws IOException {
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allPackages().setMinima(40, 50, 40)));
        assertOutput(analyzer.analyze(), "" +
                "org.springframework.config                                   81 / 40      87 / 50      19 / 40     \n" +
                "org.springframework.data.neo4j.repository.query              70 / 40      78 / 50      39 / 40     \n" +
                "org.springframework.event                                    80 / 40      80 / 50      24 / 40     \n" +
                "org.springframework.persist                                  19 / 40      19 / 50      52 / 40     ");
    }

    @Test
    public void packageClasses() throws IOException {
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allInPackage("org.springframework.handler").setMinima(40, 50, 40)));
        assertOutput(analyzer.analyze(), "" +
                "org.springframework.handler.InputPersister                   24 / 40      50 / 50      40 / 40     \n" +
                "org.springframework.handler.MailReceiver                      7 / 40      40 / 50      33 / 40     ");
    }

    @Test
    public void classes() throws IOException {
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.allClasses().setMinima(30, 30, 30)));
        assertOutput(analyzer.analyze(), "" +
                "org.springframework.config.MailSparkConverter                 7 / 30      50 / 30      50 / 30     \n" +
                "org.springframework.handler.InputPersister                   24 / 30      50 / 30      40 / 30     \n" +
                "org.springframework.handler.MailReceiver                      7 / 30      40 / 30      33 / 30     \n" +
                "org.springframework.parsing.SingleLineValueRetriever.Boundar  0 / 30       0 / 30       0 / 30     \n" +
                "org.springframework.parsing.strategy.algo.BodyParser          0 / 30       0 / 30       0 / 30     \n" +
                "org.springframework.persist.JsonRepositoryImpl                8 / 30       6 / 30       7 / 30     \n" +
                "org.springframework.persist.TransactionalEventBus            19 / 30      12 / 30      13 / 30     ");
    }

    @Test
    public void explicit() throws IOException {
        final JacocoAnalyzer analyzer = new JacocoAnalyzer(new CoverageCollector(INSTRUCTION, METHOD, COMPLEXITY)
                .just(For.clazz("org.springframework.handler.InputPersister").setMinima(30, 30, 30))
                .just(For.packge("org.springframework.event").setMinima(30, 30, 30)));
        assertOutput(analyzer.analyze(), "" +
                "org.springframework.event                                    80 / 30      80 / 30      24 / 30     \n" +
                "org.springframework.handler.InputPersister                   24 / 30      50 / 30      40 / 30     ");
    }

    private void assertOutput(JacocoResult result, String expected) {
        final StringDescription sd = new StringDescription();
        new CoverageMatcher().describeMismatchSafely(result, sd);
        assertThat(sd.toString(), equalTo("Analyzed coverage types:                                    INSTRUCTION  METHOD       COMPLEXITY   \n" + expected));
    }
}
