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
package guru.nidi.codeassert.util;

import java.util.List;

/**
 *
 */
public class LocationMatcher {
    private final List<String> locs;
    private final List<String> names;

    public LocationMatcher(List<String> locs, List<String> names) {
        this.locs = locs;
        this.names = names;
    }

    public boolean matches(String name, String className, String method) {
        if (locs.isEmpty()) {
            return names.contains(name);
        }
        for (final String loc : locs) {
            if (matches(loc, name, className, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(String loc, String name, String className, String method) {
        if (!names.contains(name)) {
            return false;
        }
        final int methodPos = loc.indexOf('#');
        if (methodPos < 0) {
            return matchClass(className, loc);
        }
        if (methodPos == 0) {
            return matchMethod(method, loc.substring(1));
        }
        return matchClass(className, loc.substring(0, methodPos)) && matchMethod(method, loc.substring(methodPos + 1));
    }

    private boolean matchMethod(String testMethod, String method) {
        return method.equals(testMethod);
    }

    private boolean matchClass(String testClass, String clazz) {
        final int pos = testClass.lastIndexOf('.');
        return clazz.contains(".") || pos < 0
                ? clazz.equals(testClass)
                : clazz.equals(testClass.substring(pos + 1));
    }
}