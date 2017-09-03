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
import java.io.IOException;
import java.util.*;

/**
 * The <code>FileManager</code> class is responsible for extracting
 * Java class files (<code>.class</code> files) from a collection of
 * registered directories.
 *
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */
class FileManager {
    private final List<File> files = new ArrayList<>();

    FileManager withFile(String name) throws IOException {
        final File file = new File(name);
        if (!file.isFile() && !acceptJarFile(file)) {
            throw new IOException("Invalid directory or JAR file: " + name);
        }
        files.add(file);
        return this;
    }

    boolean acceptClassFile(File file) {
        return file.isFile() && acceptClassFileName(file.getName());
    }

    boolean acceptClassFileName(String name) {
        return name.toLowerCase(Locale.ENGLISH).endsWith(".class");
    }

    boolean acceptJarFile(File file) {
        return isJar(file) || isZip(file) || isWar(file);
    }

    Collection<File> extractFiles() {
        final Collection<File> res = new TreeSet<>();
        res.addAll(files);
        return res;
    }

    private boolean isWar(File file) {
        return existsWithExtension(file, ".war");
    }

    private boolean isZip(File file) {
        return existsWithExtension(file, ".zip");
    }

    private boolean isJar(File file) {
        return existsWithExtension(file, ".jar");
    }

    private boolean existsWithExtension(File file, String extension) {
        return file.isFile() && file.getName().toLowerCase(Locale.ENGLISH).endsWith(extension);
    }

}
