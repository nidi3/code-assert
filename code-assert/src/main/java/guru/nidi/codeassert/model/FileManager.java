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

public class FileManager {
    private final List<File> directories = new ArrayList<>();

    public FileManager withDirectories(List<String> names) throws IOException {
        for (final String name : names) {
            withDirectory(name);
        }
        return this;
    }

    public FileManager withDirectory(String name) throws IOException {
        final File directory = new File(name);
        if (directory.isDirectory() || acceptJarFile(directory)) {
            directories.add(directory);
        } else {
            throw new IOException("Invalid directory or JAR file: " + name);
        }
        return this;
    }

    public boolean acceptFile(File file) {
        return acceptClassFile(file) || acceptJarFile(file);
    }

    public boolean acceptClassFile(File file) {
        return file.isFile() && acceptClassFileName(file.getName());
    }

    public boolean acceptClassFileName(String name) {
        return name.toLowerCase(Locale.ENGLISH).endsWith(".class");
    }

    public boolean acceptJarFile(File file) {
        return isJar(file) || isZip(file) || isWar(file);
    }

    public Collection<File> extractFiles() {
        final Collection<File> files = new TreeSet<>();
        for (final File directory : directories) {
            collectFiles(directory, files);
        }
        return files;
    }

    private void collectFiles(File directory, Collection<File> files) {
        if (directory.isFile()) {
            addFile(directory, files);
        } else {
            final File[] directoryFiles = directory.listFiles();
            if (directoryFiles != null) {
                for (final File file : directoryFiles) {
                    if (acceptFile(file)) {
                        addFile(file, files);
                    } else if (file.isDirectory()) {
                        collectFiles(file, files);
                    }
                }
            }
        }
    }

    private void addFile(File f, Collection<File> files) {
        if (!files.contains(f)) {
            files.add(f);
        }
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
