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
import java.util.List;

/**
 *
 */
public abstract class BaseCollector<S, T extends BaseCollector<S, T>> {
    public T because(String reason, Action... actions) {
        return config(CollectorConfig.because(reason, actions));
    }

    public T just(Action... actions) {
        return config(CollectorConfig.just(actions));
    }

    public abstract T config(final CollectorConfig... configs);

    public boolean accept(Issue<S> issue) {
        return issue.accept(this, null);
    }

    protected abstract boolean doAccept(S issue, Action action);

    protected abstract boolean doAccept(S issue);

    public abstract List<Action> unused(RejectCounter counter);

    protected boolean accept(Issue<S> issue, T parent, CollectorConfig... configs) {
        for (final CollectorConfig config : configs) {
            for (final Action action : config.actions) {
                if (!issue.accept(this, action)) {
                    return false;
                }
            }
        }
        return parent.accept(issue);
    }

    protected List<Action> unused(RejectCounter counter, T parent, CollectorConfig... configs) {
        final List<Action> res = new ArrayList<>();
        for (final CollectorConfig config : configs) {
            for (final Action action : config.actions) {
                if (counter.getCount(action) == 0) {
                    res.add(action);
                }
            }
        }
        res.addAll(parent.unused(counter));
        return res;
    }

    public List<String> unusedActions(RejectCounter counter) {
        final List<String> res = new ArrayList<>();
        for (final Action unused : unused(counter)) {
            res.add(unused == null ? "    Base filtering" : unused.toString());
        }
        return res;
    }

    public void printUnusedWarning(RejectCounter counter) {
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
}