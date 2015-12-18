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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public abstract class BaseProject<T> {
    protected final List<String> codeLocations;
    protected final PackageCollector packageCollector;

    public BaseProject(String codeLocation, PackageCollector collector) {
        this(Collections.singletonList(codeLocation), collector);
    }

    public BaseProject(BaseProject<T> project) {
        this(project.codeLocations, project.packageCollector);
    }

    public BaseProject(List<String> codeLocations, PackageCollector collector) {
        this.codeLocations = codeLocations;
        this.packageCollector = collector;
    }

    public abstract T analyze() throws IOException;
}