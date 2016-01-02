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
package guru.nidi.codeassert.pmd;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class PmdMatchers {
    private PmdMatchers() {
    }

    public static Matcher<PmdAnalyzer> hasNoPmdViolations() {
        return new PmdMatcher();
    }

    public static Matcher<CpdAnalyzer> hasNoDuplications() {
        return new CpdMatcher();
    }

    private static class PmdMatcher extends TypeSafeMatcher<PmdAnalyzer> {
        private List<RuleViolation> violations;

        @Override
        protected boolean matchesSafely(PmdAnalyzer item) {
            violations = item.analyze();
            return violations.isEmpty();
        }

        public void describeTo(Description description) {
            description.appendText("Has no PMD issues");
        }

        @Override
        protected void describeMismatchSafely(PmdAnalyzer item, Description description) {
            for (final RuleViolation violation : violations) {
                description.appendText("\n").appendText(printViolation(violation));
            }
        }

        private String printViolation(RuleViolation violation) {
            final Rule rule = violation.getRule();
            return String.format("%-11s %-45s %s:%d    %s",
                    rule.getPriority(), rule.getName(), violation.getFilename(), violation.getBeginLine(), violation.getDescription());
        }
    }

    private static class CpdMatcher extends TypeSafeMatcher<CpdAnalyzer> {
        private List<Match> matches;

        @Override
        protected boolean matchesSafely(CpdAnalyzer item) {
            matches = item.analyze();
            return matches.isEmpty();
        }

        public void describeTo(Description description) {
            description.appendText("Has no code duplications");
        }

        @Override
        protected void describeMismatchSafely(CpdAnalyzer item, Description description) {
            for (final Match match : matches) {
                description.appendText("\n").appendText(printMatch(match));
            }
        }

        private String printMatch(Match match) {
            final StringBuilder s = new StringBuilder();
            boolean first = true;
            for (final Iterator<Mark> marks = match.iterator(); marks.hasNext(); ) {
                final Mark mark = marks.next();
                s.append(first ? String.format("%-4d ", match.getTokenCount()) : "     ");
                first = false;
                s.append(String.format("%s:%d-%d%n", mark.getFilename(), mark.getBeginLine(), mark.getEndLine()));
            }
            return s.substring(0, s.length() - 1);
        }
    }

}
