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
package guru.nidi.codeassert;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class AnalyzerConfig {
    private final List<String> codeLocations;
    private final PackageCollector packageCollector;

    public AnalyzerConfig(String... codeLocations) {
        this(Arrays.asList(codeLocations), PackageCollector.all());
    }

    public AnalyzerConfig(AnalyzerConfig config) {
        this(config.codeLocations, config.packageCollector);
    }

    protected AnalyzerConfig(List<String> codeLocations, PackageCollector collector) {
        this.codeLocations = codeLocations;
        this.packageCollector = collector;
    }

    public static AnalyzerConfig mavenMainClasses(String... packages) {
        return new AnalyzerConfig(prepend("target/classes/", packages));
    }


    public static AnalyzerConfig mavenTestClasses(String... packages) {
        return new AnalyzerConfig(prepend("target/test-classes/", packages));
    }

    public static AnalyzerConfig mavenMainAndTestClasses(String... packages) {
        return new AnalyzerConfig(merge(prepend("target/classes/", packages), prepend("target/test-classes/", packages)));
    }

    private static String[] prepend(String prefix, String[] ss) {
        if (ss.length == 0) {
            return new String[]{prefix};
        }
        final String[] res=new String[ss.length];
        for (int i = 0; i < ss.length; i++) {
            res[i] = prefix + ss[i];
        }
        return res;
    }

    private static String[] merge(String[] ss1, String[] ss2) {
        final String[] res = new String[ss1.length + ss2.length];
        System.arraycopy(ss1, 0, res, 0, ss1.length);
        System.arraycopy(ss2, 0, res, ss1.length, ss2.length);
        return res;
    }

    public AnalyzerConfig collecting(PackageCollector collector) {
        return new AnalyzerConfig(codeLocations, collector);
    }

    public List<String> getCodeLocations() {
        return codeLocations;
    }

    public PackageCollector getPackageCollector() {
        return packageCollector;
    }
}