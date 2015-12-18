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

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class AnalyzerConfig {
    private final List<String> codeLocations;
    private final PackageCollector packageCollector;

    public AnalyzerConfig(String codeLocation, PackageCollector collector) {
        this(Collections.singletonList(codeLocation), collector);
    }

    public AnalyzerConfig(AnalyzerConfig config) {
        this(config.codeLocations, config.packageCollector);
    }

    public AnalyzerConfig(List<String> codeLocations, PackageCollector collector) {
        this.codeLocations = codeLocations;
        this.packageCollector = collector;
    }

    public List<String> getCodeLocations() {
        return codeLocations;
    }

    public PackageCollector getPackageCollector() {
        return packageCollector;
    }
}