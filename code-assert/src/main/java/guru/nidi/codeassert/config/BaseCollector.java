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
package guru.nidi.codeassert.config;

import guru.nidi.codeassert.util.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class BaseCollector<S, A extends Action, T extends BaseCollector<S, A, T>> {
    private static final Logger LOG = LoggerFactory.getLogger(BaseCollector.class);

    @SafeVarargs
    public final T because(String reason, A... actions) {
        return config(CollectorConfig.because(reason, actions));
    }

    @SafeVarargs
    public final T just(A... actions) {
        return config(CollectorConfig.just(actions));
    }

    protected abstract T config(CollectorConfig<A>... configs);

    public T apply(Iterable<CollectorConfig<A>> configs) {
        final List<CollectorConfig<A>> cs = new ArrayList<>();
        for (final CollectorConfig<A> config : configs) {
            cs.add(config);
        }
        return (T) config(cs.toArray(new CollectorConfig[cs.size()]));
    }

    public abstract ActionResult accept(S issue);

    protected abstract ActionResult doAccept(S issue, A action);

    protected abstract List<A> unused(UsageCounter counter);

    @SafeVarargs
    protected final ActionResult accept(S issue, T parent, CollectorConfig<A>... configs) {
        ActionResult res = ActionResult.undecided(null);
        for (final CollectorConfig<A> config : configs) {
            for (final A action : config.actions) {
                res = res.orMoreQuality(doAccept(issue, action));
            }
        }
        return res.orMoreQuality(parent.accept(issue));
    }

    @SafeVarargs
    protected final List<A> unused(UsageCounter counter, T parent, CollectorConfig<A>... configs) {
        final List<A> res = new ArrayList<>();
        for (final CollectorConfig<A> config : configs) {
            if (!config.ignoreUnused) {
                for (final A action : config.actions) {
                    if (counter.getCount(action) == 0) {
                        res.add(action);
                    }
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
                final boolean projectClass = !clazz.startsWith("guru.nidi.codeassert")
                        && !clazz.startsWith("org.hamcrest")
                        && !clazz.startsWith("org.junit");
                final boolean testClass = clazz.endsWith("Test") || clazz.startsWith("Test");
                if (projectClass || testClass) {
                    break;
                }
            }
            final String location = i == trace.length ? ""
                    : ("In " + trace[i].getClassName() + "#" + trace[i].getMethodName() + ": ");
            LOG.warn(location + "These collector actions have not been used:\n" + s);
        }
    }

    protected List<A> unusedNullAction(UsageCounter counter, boolean hasDefaultConfig) {
        return counter.getCount(null) == 0 && hasDefaultConfig
                ? Collections.<A>singletonList(null)
                : Collections.<A>emptyList();
    }

    protected String guessClassFromFile(String filename, Language language) {
        final String file = filename.replace('\\', '/');
        //TODO can this heuristic be improved?
        final int slash = file.lastIndexOf('/');
        final int dot = file.lastIndexOf('.');
        final int src = file.indexOf("/src/") + 4;
        final int lang = file.indexOf("/" + language.path + "/") + language.path.length() + 1;
        final int later = Math.max(src, lang);
        final int start = later >= 5 ? later + 1 : slash + 1;
        return file.substring(start, dot).replace('/', '.');
    }
}
