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
package guru.nidi.codeassert.ktlint;

import guru.nidi.codeassert.config.*;

import java.util.List;

import static guru.nidi.codeassert.util.ListUtils.andJoin;

public class KtlintCollector extends BaseCollector<LocatedLintError, Ignore, KtlintCollector> {
    @Override
    public KtlintCollector config(final CollectorConfig<Ignore>... configs) {
        return new KtlintCollector() {
            @Override
            public ActionResult accept(LocatedLintError issue) {
                return accept(issue, KtlintCollector.this, configs);
            }

            public List<Ignore> unused(UsageCounter counter) {
                return unused(counter, KtlintCollector.this, configs);
            }

            @Override
            public String toString() {
                return andJoin(KtlintCollector.this, configs);
            }
        };
    }

    @Override
    public ActionResult accept(LocatedLintError issue) {
        return ActionResult.accept(null, 1);
    }

    @Override
    protected ActionResult doAccept(LocatedLintError issue, Ignore action) {
        final String className = guessClassFromFile(issue.file.getAbsolutePath(), Language.KOTLIN);
        final Language language = Language.byFilename(issue.file.getName());
        return action.accept(new NamedLocation(issue.ruleId, language, className, null, true));
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
