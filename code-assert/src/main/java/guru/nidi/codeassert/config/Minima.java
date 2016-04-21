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
        for (int value : values) {
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
        return pattern.equals(value) || (pattern.equals("*") && (wcMatchEmpty || value.length() > 0));
    }

    @Override
    public String toString() {
        return "    minima for " + pack + "." + clazz + ": " + Arrays.toString(values);
    }
}
