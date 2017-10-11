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
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;

import java.util.List;

public class StyleEventCollector extends BaseCollector<AuditEvent, Ignore, StyleEventCollector> {
    private final SeverityLevel severity;

    public StyleEventCollector() {
        this(null);
    }

    private StyleEventCollector(SeverityLevel severity) {
        this.severity = severity;
    }

    public StyleEventCollector severity(SeverityLevel severity) {
        return new StyleEventCollector(severity);
    }

    @SafeVarargs
    @Override
    public final StyleEventCollector config(final CollectorConfig<Ignore>... configs) {
        return new StyleEventCollector(severity) {
            @Override
            public ActionResult accept(AuditEvent issue) {
                return accept(issue, StyleEventCollector.this, configs);
            }

            public List<Ignore> unused(UsageCounter counter) {
                return unused(counter, StyleEventCollector.this, configs);
            }

            @Override
            public String toString() {
                return StyleEventCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public ActionResult accept(AuditEvent issue) {
        return new ActionResult(severity == null || issue.getSeverityLevel().ordinal() >= severity.ordinal(), null, 1);
    }

    @Override
    protected ActionResult doAccept(AuditEvent issue, Ignore action) {
        final String className = guessClassFromFile(issue.getFileName(), Language.JAVA);
        final String name = issue.getLocalizedMessage().getKey();
        return action.accept(new NamedLocation(name, Language.byFilename(issue.getFileName()), className, "", false));
    }

    @Override
    public List<Ignore> unused(UsageCounter counter) {
        return unusedNullAction(counter, severity != null);
    }

    @Override
    public String toString() {
        return (severity == null ? "" : ("Severity >= " + severity));
    }

}
