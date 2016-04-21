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

import java.util.Arrays;

/**
 *
 */
public class Minima implements Action<ValuedLocation> {
    private final String pack;
    private final String clazz;
    private final int[] values;

    Minima(String pack, String clazz, int... values) {
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
    public boolean accept(ValuedLocation valLoc) {
        if (!wildcardMatches(pack, valLoc.pack, false) || !wildcardMatches(clazz, valLoc.clazz, false)) {
            return false;
        }
        for (int i = 0; i < valLoc.values.length; i++) {
            valLoc.appliedLimits[i] = i < values.length ? values[i] : -1;
        }
        for (int i = 0; i < valLoc.values.length; i++) {
            if (valLoc.values[i] < valLoc.appliedLimits[i]) {
                return true;
            }
        }
        return false;
    }

    private boolean wildcardMatches(String pattern, String value, boolean wcMatchEmpty) {
        return pattern.equals(value) || ("*".equals(pattern) && (wcMatchEmpty || value.length() > 0));
    }

    @Override
    public String toString() {
        return "    minima for " + pack + "." + clazz + ": " + Arrays.toString(values);
    }
}
