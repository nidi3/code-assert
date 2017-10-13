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

import guru.nidi.codeassert.config.*;
import guru.nidi.codeassert.util.ListUtils;

import java.io.File;
import java.util.List;

import static guru.nidi.codeassert.config.Language.KOTLIN;

public class DetektCollector extends BaseCollector<TypedDetektFinding, Ignore, DetektCollector> {
    @Override
    public DetektCollector config(final CollectorConfig<Ignore>... configs) {
        return new DetektCollector() {
            @Override
            public ActionResult accept(TypedDetektFinding issue) {
                return accept(issue, DetektCollector.this, configs);
            }

            public List<Ignore> unused(UsageCounter counter) {
                return unused(counter, DetektCollector.this, configs);
            }

            @Override
            public String toString() {
                return DetektCollector.this.toString() + "\n" + ListUtils.join("\n", configs);
            }
        };
    }

    @Override
    public ActionResult accept(TypedDetektFinding issue) {
        return ActionResult.accept(null, 1);
    }

    @Override
    protected ActionResult doAccept(TypedDetektFinding issue, Ignore action) {
        final File file = new File(issue.basedir, issue.entity.getLocation().getFile());
        final String className = guessClassFromFile(file.getAbsolutePath(), KOTLIN);
        return action.accept(new NamedLocation(issue.name, KOTLIN, className, null, true));
    }

    @Override
    public List<Ignore> unused(UsageCounter counter) {
        return unusedNullAction(counter, false);
    }

    @Override
    public String toString() {
        return "";
    }

}
