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

import guru.nidi.codeassert.config.ValuedLocation;
import guru.nidi.codeassert.util.ResultMatcher;
import org.hamcrest.Description;

public class CoverageMatcher extends ResultMatcher<JacocoResult, ValuedLocation> {
    public void describeTo(Description description) {
        description.appendText("Has enough test coverage.");
    }

    @Override
    protected void describeMismatchSafely(JacocoResult item, Description description) {
        description.appendText("Found unsatisfied test coverage requirements:\n");
        description.appendText(pad("Analyzed coverage types:", 60));
        for (final CoverageType type : item.getTypes()) {
            description.appendText(pad(type.toString(), 13));
        }
        for (final ValuedLocation coverage : item.findings()) {
            description.appendText("\n");
            printCoverage(coverage, description);
        }
    }

    private void printCoverage(ValuedLocation coverage, Description description) {
        description.appendText(pad(printLocation(coverage), 60));
        for (int i = 0; i < coverage.getValues().length; i++) {
            description.appendText(String.format("%3.0f /%3s     ",
                    coverage.getValues()[i], printAppliedValue(coverage.getAppliedLimits()[i])));
        }
    }

    private String printAppliedValue(double value) {
        return value == -1
                ? " na"
                : String.format("%3.0f", value);
    }

    private String printLocation(ValuedLocation coverage) {
        if (coverage.getClazz().length() == 0) {
            if (coverage.getPack().length() == 0) {
                return "<global>";
            }
            return coverage.getPack();
        }
        return coverage.getPack() + "." + coverage.getClazz();
    }

    private String pad(String s, int len) {
        if (s.length() > len) {
            return s.substring(0, len);
        }
        final StringBuilder p = new StringBuilder(s);
        while (p.length() < len) {
            p.append(' ');
        }
        return p.toString();
    }
}
