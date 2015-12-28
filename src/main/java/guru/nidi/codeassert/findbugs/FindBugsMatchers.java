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

import java.util.Collection;

/**
 *
 */
public class FindBugsMatchers {
    private FindBugsMatchers() {
    }

    public static Matcher<FindBugsAnalyzer> findsNoBugs() {
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
                description.appendText("\n").appendText(printBug(bug));
            }
        }

        private String printBug(BugInstance bug) {
            final int rank = BugRanker.findRank(bug);
            final SourceLineAnnotation line = bug.getPrimarySourceLineAnnotation();
            final String msg = bug.getMessage();
            final int pos = msg.indexOf(':');
            final String message = msg.substring(pos + 2).replace('\n', ' ');
            return String.format("%-2d %-8s %-45s %s:%d    %s", rank, priority(bug), bug.getType(), line.getClassName(), line.getStartLine(), message);
        }

        private String priority(BugInstance bug) {
            switch (bug.getPriority()) {
                case Priorities.EXP_PRIORITY:
                    return "E";
                case Priorities.LOW_PRIORITY:
                    return "L";
                case Priorities.NORMAL_PRIORITY:
                    return "M";
                case Priorities.HIGH_PRIORITY:
                    return "H";
                default:
                    return "?";
            }
        }
    }

}
