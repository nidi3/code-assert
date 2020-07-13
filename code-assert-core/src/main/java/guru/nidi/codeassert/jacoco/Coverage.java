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
package guru.nidi.codeassert.jacoco;

import guru.nidi.codeassert.config.ValuedLocation;

public class Coverage implements Comparable<Coverage> {
    final String pack;
    private final String clazz;
    private final int instMissed, instCovered;
    private final int branchMissed, branchCovered;
    private final int lineMissed, lineCovered;
    private final int complexMissed, complexCovered;
    private final int methodMissed, methodCovered;

    public Coverage(String pack, String clazz,
                    int instMissed, int instCovered, int branchMissed, int branchCovered, int lineMissed,
                    int lineCovered, int complexMissed, int complexCovered, int methodMissed, int methodCovered) {
        this.pack = pack;
        this.clazz = clazz;
        this.instMissed = instMissed;
        this.instCovered = instCovered;
        this.branchMissed = branchMissed;
        this.branchCovered = branchCovered;
        this.lineMissed = lineMissed;
        this.lineCovered = lineCovered;
        this.complexMissed = complexMissed;
        this.complexCovered = complexCovered;
        this.methodMissed = methodMissed;
        this.methodCovered = methodCovered;
    }

    public Coverage combined(Coverage c) {
        return new Coverage(pack.equals(c.pack) ? pack : "", clazz.equals(c.clazz) ? clazz : "",
                instMissed + c.instMissed, instCovered + c.instCovered,
                branchMissed + c.branchMissed, branchCovered + c.branchCovered,
                lineMissed + c.lineMissed, lineCovered + c.lineCovered,
                complexCovered + c.complexCovered, complexMissed + c.complexMissed,
                methodMissed + c.methodMissed, methodCovered + c.methodCovered);
    }

    public Coverage withClazz(String clazz) {
        return new Coverage(pack, clazz, instMissed, instCovered, branchMissed, branchCovered, lineMissed, lineCovered,
                complexMissed, complexCovered, methodMissed, methodCovered);
    }

    public ValuedLocation toValuedLocation(CoverageType[] types) {
        return new ValuedLocation(pack, clazz, projection(types));
    }

    public double[] projection(CoverageType[] types) {
        final double[] res = new double[types.length];
        for (int i = 0; i < types.length; i++) {
            res[i] = projection(types[i]);
        }
        return res;
    }

    public double projection(CoverageType type) {
        switch (type) {
            case INSTRUCTION:
                return instCoverage();
            case BRANCH:
                return branchCoverage();
            case LINE:
                return lineCoverage();
            case COMPLEXITY:
                return complexCoverage();
            case METHOD:
                return methodCoverage();
            default:
                throw new AssertionError("Unhandled CoverageType " + type);
        }
    }

    public double instCoverage() {
        return cover(instMissed, instCovered);
    }

    public double branchCoverage() {
        return cover(branchMissed, branchCovered);
    }

    public double lineCoverage() {
        return cover(lineMissed, lineCovered);
    }

    public double complexCoverage() {
        return cover(complexMissed, complexCovered);
    }

    public double methodCoverage() {
        return cover(methodMissed, methodCovered);
    }

    private double cover(int missed, int covered) {
        final int sum = missed + covered;
        return sum == 0 ? 100 : 100d * covered / sum;
    }

    @Override
    public int compareTo(Coverage o) {
        return (pack + "." + clazz).compareToIgnoreCase(o.pack + "." + o.clazz);
    }

}
