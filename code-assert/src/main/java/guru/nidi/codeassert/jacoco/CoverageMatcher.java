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

import guru.nidi.codeassert.config.ValuedLocation;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 *
 */
public class CoverageMatcher extends TypeSafeMatcher<JacocoResult> {
    @Override
    protected boolean matchesSafely(JacocoResult item) {
        return item.findings().isEmpty();
    }

    public void describeTo(Description description) {
        description.appendText("Has no coverage issues");
    }

    @Override
    protected void describeMismatchSafely(JacocoResult item, Description description) {
        String s = pad("Analyzed coverage types:", 60);
        for (final CoverageType type : item.getTypes()) {
            s += pad(type.toString(), 13);
        }
        description.appendText(s);
        for (final ValuedLocation coverage : item.findings()) {
            description.appendText("\n").appendText(printCoverage(coverage));
        }
    }

    private String printCoverage(ValuedLocation coverage) {
        String s = pad(printLocation(coverage), 60);
        for (int i = 0; i < coverage.getValues().length; i++) {
            s += String.format("%3.0f /%3s     ", coverage.getValues()[i], printAppliedValue(coverage.getAppliedLimits()[i]));
        }
        return s;
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
        while (s.length() < len) {
            s += " ";
        }
        return s;
    }
}
