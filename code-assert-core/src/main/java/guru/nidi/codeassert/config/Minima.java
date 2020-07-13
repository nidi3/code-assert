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

import java.util.Arrays;

public class Minima implements Action<ValuedLocation> {
    private final LocationMatcher locationMatcher;
    private final String pack;
    private final String clazz;
    private final int[] values;

    Minima(Location loc, String pack, String clazz, int... values) {
        locationMatcher = loc == null ? null : new LocationMatcher(loc);
        this.pack = pack;
        this.clazz = clazz;
        for (final int value : values) {
            if (value < 0 || value > 100) {
                throw new IllegalArgumentException("Invalid value " + value);
            }
        }
        this.values = values;
    }

    public int getValueCount() {
        return values.length;
    }

    @Override
    public ActionResult accept(ValuedLocation valLoc) {
        final int quality = matchLocation(valLoc);
        if (quality == 0) {
            return ActionResult.undecided(this);
        }

        for (int i = 0; i < valLoc.values.length; i++) {
            valLoc.appliedLimits[i] = i < values.length ? values[i] : -1;
        }
        for (int i = 0; i < valLoc.values.length; i++) {
            if (valLoc.values[i] < valLoc.appliedLimits[i]) {
                return ActionResult.accept(this, quality);
            }
        }
        return ActionResult.reject(this, quality);
    }

    private int matchLocation(ValuedLocation valLoc) {
        if (locationMatcher == null) {
            final int packMatch = wildcardMatches(pack, valLoc.pack);
            final int clazzMatch = wildcardMatches(clazz, valLoc.clazz);
            if (packMatch == 0 || clazzMatch == 0) {
                return 0;
            }
            return packMatch + clazzMatch;
        }
        if (!locationMatcher.matchesPackageClass(valLoc.pack, valLoc.clazz)) {
            return 0;
        }
        return locationMatcher.specificity();
    }

    private int wildcardMatches(String pattern, String value) {
        if (pattern.equals(value)) {
            return 3;
        }
        if (complexWildcardMatches(pattern, value)) {
            return 2;
        }
        if ("*".equals(pattern) && value.length() > 0) {
            return 1;
        }
        return 0;
    }

    private boolean complexWildcardMatches(String pattern, String value) {
        if (pattern.length() <= 1) {
            return false;
        }
        final boolean startWild = pattern.startsWith("*") && value.endsWith(pattern.substring(1));
        final boolean endWild = pattern.endsWith("*") && value.startsWith(pattern.substring(0, pattern.length() - 1));
        return startWild || endWild;
    }

    @Override
    public String toString() {
        return "    minima for " + pack + "." + clazz + ": " + Arrays.toString(values);
    }
}
