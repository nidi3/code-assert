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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The <code>PackageFilter</code> class is used to filter imported
 * package names.
 * A Package Filter is constructed like this:
 * <pre>PackageFilter filter = PackageFilter.all().excluding(...).including(...)...</pre>
 * The filter executes all excluding/including entries in the order they are defined.
 * The first one that matches is used as the result of the filter.
 * If no entry matches, the Filter accepts a package.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class PackageCollector {
    private final Collection<Collector> collectors;

    private PackageCollector(Collection<Collector> collectors) {
        this.collectors = collectors;
    }

    private PackageCollector() {
        this(new ArrayList<Collector>());
    }

    public static PackageCollector all() {
        return new PackageCollector();
    }

    public PackageCollector excluding(String... packageNames) {
        return add(Arrays.asList(packageNames), false);
    }

    public PackageCollector excluding(Collection<String> packageNames) {
        return add(packageNames, false);
    }

    public PackageCollector excludingRest() {
        return excluding("");
    }

    public PackageCollector including(String... packageNames) {
        return add(Arrays.asList(packageNames), true);
    }

    public PackageCollector including(Collection<String> packageNames) {
        return add(packageNames, true);
    }

    /**
     * Indicates whether the specified package name passes this package filter.
     *
     * @param packageName Package name.
     * @return <code>true</code> if the package name should be included;
     * <code>false</code> otherwise.
     */
    public boolean accept(String packageName) {
        for (Collector collector : collectors) {
            if (packageName.startsWith(collector.name)) {
                return collector.include;
            }
        }
        return true;
    }

    private PackageCollector add(Collection<String> packageNames, boolean include) {
        for (final String packageName : packageNames) {
            add(packageName, include);
        }
        return this;
    }

    private PackageCollector add(String packageName, boolean include) {
        if (packageName.endsWith("*")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }
        collectors.add(new Collector(packageName, include));
        return this;
    }

    public Collection<Collector> getCollectors() {
        return collectors;
    }

    public static class Collector {
        public final String name;
        public final boolean include;

        public Collector(String name, boolean include) {
            this.name = name;
            this.include = include;
        }
    }
}
