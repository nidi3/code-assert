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
package guru.nidi.codeassert.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ModelBuilder {
    private final List<File> files = new ArrayList<>();
    private final List<String> ignorePackages = new ArrayList<>();
    private final List<String> mergePackages = new ArrayList<>();

    public static ModelBuilder from(File... files) {
        return from(asList(files));
    }

    public static ModelBuilder from(List<File> files) {
        return new ModelBuilder().and(files);
    }

    public ModelBuilder and(File... files) {
        return and(asList(files));
    }

    public ModelBuilder and(List<File> files) {
        this.files.addAll(files);
        return this;
    }

    public ModelBuilder ignoringPackages(String... packages) {
        return ignoringPackages(asList(packages));
    }

    public ModelBuilder ignoringPackages(List<String> packages) {
        ignorePackages.addAll(packages);
        return this;
    }

    public ModelBuilder mergingPackages(String... packages) {
        return mergingPackages(asList(packages));
    }

    public ModelBuilder mergingPackages(List<String> packages) {
        mergePackages.addAll(packages);
        return this;
    }

    public Model read() {
        return new Model(ignorePackages,mergePackages).read(files);
    }
}
