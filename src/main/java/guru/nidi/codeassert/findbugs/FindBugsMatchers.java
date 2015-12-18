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

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugRanker;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/**
 *
 */
public class FindBugsMatchers {
    private FindBugsMatchers() {
    }

    public static Matcher<FindBugsAnalyzer> hasNoIssues() {
        return new FindBugsMatcher();
    }

    private static class FindBugsMatcher extends TypeSafeMatcher<FindBugsAnalyzer> {
        private Collection<BugInstance> bugs;

        @Override
        protected boolean matchesSafely(FindBugsAnalyzer item) {
            bugs = item.analyze();
            return bugs.isEmpty();
        }

        public void describeTo(Description description) {
            description.appendText("Has no FindBugs issues");
        }

        @Override
        protected void describeMismatchSafely(FindBugsAnalyzer item, Description description) {
            for (final BugInstance bug : bugs) {
                description.appendText(printBug(bug)).appendText("\n");
            }
        }

        private String printBug(BugInstance bug) {
            final StringWriter sw = new StringWriter();
            final PrintWriter out = new PrintWriter(sw);
            int rank = BugRanker.findRank(bug);
            out.printf("%2d ", rank);

            switch (bug.getPriority()) {
                case Priorities.EXP_PRIORITY:
                    out.print("E");
                    break;
                case Priorities.LOW_PRIORITY:
                    out.print("L");
                    break;
                case Priorities.NORMAL_PRIORITY:
                    out.print("M");
                    break;
                case Priorities.HIGH_PRIORITY:
                    out.print("H");
                    break;
                default:
                    assert false;
            }

            out.printf(" %-40s ", bug.getType());

            SourceLineAnnotation line = bug.getPrimarySourceLineAnnotation();
            final String msg = bug.getMessage();
            final int pos = msg.indexOf(':');
            out.print(msg.substring(pos + 2).replace('\n', ' ') + "  " + line.toString());
            return sw.toString();
        }
    }

}
