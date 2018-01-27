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
package guru.nidi.codeassert.detekt;

import guru.nidi.codeassert.util.ResultMatcher;
import io.gitlab.arturbosch.detekt.api.Location;
import org.hamcrest.Description;

import java.io.File;

public class DetektMatcher extends ResultMatcher<DetektResult, TypedDetektFinding> {
    public void describeTo(Description description) {
        description.appendText("Has no detekt issues");
    }

    @Override
    protected void describeMismatchSafely(DetektResult item, Description description) {
        for (final TypedDetektFinding error : item.findings()) {
            description.appendText("\n").appendText(printError(error));
        }
    }

    private String printError(TypedDetektFinding finding) {
        final Location location = finding.entity.getLocation();
        final String path = new File(location.getFile()).getAbsolutePath();
        return String.format("%-15s %-15s %-30s %s:%d    %s", finding.severity, finding.type, finding.name,
                path, location.getSource().getLine(), finding.description);
    }
}
