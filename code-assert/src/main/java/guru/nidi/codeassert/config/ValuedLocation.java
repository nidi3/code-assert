package guru.nidi.codeassert.config;

/**
 *
 */
public class ValuedLocation {
    final String pack;
    final String clazz;
    final double[] values;
    final double[] appliedLimits;

    public ValuedLocation(String pack, String clazz, double[] values) {
        this.pack = pack;
        this.clazz = clazz;
        this.values = values;
        appliedLimits = new double[values.length];
    }

    public String getPack() {
        return pack;
    }

    public String getClazz() {
        return clazz;
    }

    public double[] getValues() {
        return values;
    }

    public double[] getAppliedLimits() {
        return appliedLimits;
    }
}
