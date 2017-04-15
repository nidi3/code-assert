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
package guru.nidi.codeassert.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;
import guru.nidi.codeassert.util.ResultMatcher;
import org.hamcrest.Description;

public class CheckstyleMatcher extends ResultMatcher<CheckstyleResult, AuditEvent> {
    public void describeTo(Description description) {
        description.appendText("Has no Checkstyle issues");
    }

    @Override
    protected void describeMismatchSafely(CheckstyleResult item, Description description) {
        for (final AuditEvent event : item.findings()) {
            description.appendText("\n").appendText(printEvent(event));
        }
    }

    private String printEvent(AuditEvent event) {
        final LocalizedMessage msg = event.getLocalizedMessage();
        return String.format("%-8s %-40s %s:%d    %s",
                msg.getSeverityLevel(), msg.getKey(), event.getFileName(), event.getLine(), event.getMessage());
    }
}
