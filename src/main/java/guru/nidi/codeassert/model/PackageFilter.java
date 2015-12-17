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
package guru.nidi.codeassert.model;

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

public class PackageFilter {
    private final Collection<Filter> filters;

    private PackageFilter(Collection<Filter> filters) {
        this.filters = filters;
    }

    private PackageFilter() {
        this(new ArrayList<Filter>());
    }

    public static PackageFilter all() {
        return new PackageFilter();
    }

    public PackageFilter excluding(String... packageNames) {
        return add(Arrays.asList(packageNames), false);
    }

    public PackageFilter excluding(Collection<String> packageNames) {
        return add(packageNames, false);
    }

    public PackageFilter excludingRest() {
        return excluding("");
    }

    public PackageFilter including(String... packageNames) {
        return add(Arrays.asList(packageNames), true);
    }

    public PackageFilter including(Collection<String> packageNames) {
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
        for (Filter filter : filters) {
            if (packageName.startsWith(filter.name)) {
                return filter.include;
            }
        }
        return true;
    }

    private PackageFilter add(Collection<String> packageNames, boolean include) {
        for (final String packageName : packageNames) {
            add(packageName, include);
        }
        return this;
    }

    private PackageFilter add(String packageName, boolean include) {
        if (packageName.endsWith("*")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }
        filters.add(new Filter(packageName, include));
        return this;
    }

    public Collection<Filter> getFilters() {
        return filters;
    }

    public static class Filter {
        public final String name;
        public final boolean include;

        public Filter(String name, boolean include) {
            this.name = name;
            this.include = include;
        }
    }
}
