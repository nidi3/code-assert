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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.MethodAnnotation;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class BugCollector {
    public static BugCollector simple(final Integer maxRank, final Integer minPriority, final String... ignoredTypes) {
        return new BugCollector() {
            private List<String> ignored = Arrays.asList(ignoredTypes);

            @Override
            public boolean accept(BugInstance bug) {
                return (maxRank == null || bug.getBugRank() <= maxRank) &&
                        (minPriority == null || bug.getPriority() <= minPriority) &&
                        !ignored.contains(bug.getType());
            }
        };
    }

    public BugCollector andIgnore(final Class<?> clazz, final String... types) {
        return andIgnore(clazz.getName(), types);
    }

    /**
     * @param loc   the class/method for which the ignore should be applied.
     *              Has the form [package.][class][#method]
     * @param types
     * @return
     */
    public BugCollector andIgnore(final String loc, final String... types) {
        return new BugCollector() {
            private List<String> ignored = Arrays.asList(types);

            @Override
            public boolean accept(BugInstance bug) {
                return BugCollector.this.accept(bug) &&
                        (!matches(bug) || !ignored.contains(bug.getType()));
            }

            private boolean matches(BugInstance bug) {
                final int methodPos = loc.indexOf('#');
                if (methodPos < 0) {
                    return matchClass(bug, loc);
                } else if (methodPos == 0) {
                    return matchMethod(bug, loc.substring(1));
                } else {
                    return matchClass(bug, loc.substring(0, methodPos)) && matchMethod(bug, loc.substring(methodPos + 1));
                }
            }

            private boolean matchMethod(BugInstance bug, String method) {
                final MethodAnnotation meth = bug.getPrimaryMethod();
                return meth != null && method.equals(meth.getMethodName());
            }

            private boolean matchClass(BugInstance bug, String clazz) {
                final String className = bug.getPrimaryClass().getClassName();
                final int pos = className.lastIndexOf('.');
                return clazz.contains(".") || pos < 0
                        ? clazz.equals(className)
                        : clazz.equals(className.substring(pos + 1));
            }
        };
    }

    public boolean accept(BugInstance bug) {
        return true;
    }
}
