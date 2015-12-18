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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.Priorities;
import guru.nidi.codeassert.AnalyzerConfig;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.io.IOException;

import static guru.nidi.codeassert.PackageCollector.all;
import static guru.nidi.codeassert.findbugs.FindBugsMatchers.hasNoIssues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 */
public class FindBugsTest {
    @Test
    public void simple() throws IOException, InterruptedException {
        final FindBugsAnalyzer analyzer = new FindBugsAnalyzer(
                new AnalyzerConfig("target/classes", all()),
                BugCollector.simple(17, Priorities.NORMAL_PRIORITY));

        assertMatcher("" +
                        "17 M DLS_DEAD_LOCAL_STORE                     Dead store to accessFlags in guru.nidi.codeassert.model.ClassFileParser.parseAccessFlags()  At ClassFileParser.java:[line 159]\n" +
                        "13 M NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE   Possible null pointer dereference in guru.nidi.codeassert.model.FileManager.collectFiles(File, Collection) due to return value of called method  Dereferenced at FileManager.java:[line 83]\n",
                analyzer, hasNoIssues());
    }

    private void assertMatcher(String message, FindBugsAnalyzer analyzer, Matcher<FindBugsAnalyzer> matcher) {
        assertFalse(matcher.matches(analyzer));
        final StringDescription sd = new StringDescription();
        matcher.describeMismatch(analyzer, sd);
        assertEquals(message, sd.toString());
    }
}
