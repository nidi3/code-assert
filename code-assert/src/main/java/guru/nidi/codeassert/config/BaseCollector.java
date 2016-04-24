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
package guru.nidi.codeassert.config;

import guru.nidi.codeassert.util.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public abstract class BaseCollector<S, A extends Action, T extends BaseCollector<S, A, T>> {
    public T because(String reason, A... actions) {
        return config(CollectorConfig.because(reason, actions));
    }

    public T just(A... actions) {
        return config(CollectorConfig.just(actions));
    }

    protected abstract T config(final CollectorConfig<A>... configs);

    public abstract ActionResult accept(S issue);

    protected abstract ActionResult doAccept(S issue, A action);

    protected abstract List<A> unused(UsageCounter counter);

    protected ActionResult accept(S issue, T parent, CollectorConfig<A>... configs) {
        ActionResult res = ActionResult.undecided(null);
        for (final CollectorConfig<A> config : configs) {
            for (final A action : config.actions) {
                res = res.orMoreQuality(doAccept(issue, action));
            }
        }
        return res.orMoreQuality(parent.accept(issue));
    }

    protected List<A> unused(UsageCounter counter, T parent, CollectorConfig<A>... configs) {
        final List<A> res = new ArrayList<>();
        for (final CollectorConfig<A> config : configs) {
            for (final A action : config.actions) {
                if (counter.getCount(action) == 0) {
                    res.add(action);
                }
            }
        }
        res.addAll(parent.unused(counter));
        return res;
    }

    public List<String> unusedActions(UsageCounter counter) {
        final List<String> res = new ArrayList<>();
        for (final Action unused : unused(counter)) {
            res.add(unused == null ? "    Base filtering" : unused.toString());
        }
        return res;
    }

    public void printUnusedWarning(UsageCounter counter) {
        final String s = ListUtils.join("\n", unusedActions(counter));
        if (s.length() > 0) {
            final StackTraceElement[] trace = new Exception().fillInStackTrace().getStackTrace();
            int i;
            for (i = 0; i < trace.length; i++) {
                final String clazz = trace[i].getClassName();
                if ((!clazz.startsWith("guru.nidi.codeassert") && !clazz.startsWith("org.hamcrest") && !clazz.startsWith("org.junit"))
                        || clazz.endsWith("Test") || clazz.startsWith("Test")) {
                    break;
                }
            }
            final String location = i == trace.length ? "" : ("In " + trace[i].getClassName() + "#" + trace[i].getMethodName() + ": ");
            System.out.println("WARN: " + location + "These collector actions have not been used:");
            System.out.println(s);
        }
    }

    protected List<A> unusedNullAction(UsageCounter counter, boolean hasDefaultConfig) {
        return counter.getCount(null) == 0 && hasDefaultConfig
                ? Collections.<A>singletonList(null)
                : Collections.<A>emptyList();
    }

}