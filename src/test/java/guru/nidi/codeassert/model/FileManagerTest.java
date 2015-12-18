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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 */

public class FileManagerTest {

    private FileManager fileManager;

    @Before
    public void setUp() {
        fileManager = new FileManager();
    }

    @Test
    public void emptyFileManager() {
        assertEquals(0, fileManager.extractFiles().size());
    }

    @Test
    public void buildDirectory() throws IOException {
        fileManager.withDirectories(Arrays.asList(Path.CLASSES, Path.TEST_CLASSES));
        assertEquals(38, fileManager.extractFiles().size());
    }

    @Test(expected = IOException.class)
    public void nonExistentDirectory() throws IOException {
        fileManager.withDirectory(Path.clazz("junk").getAbsolutePath());
    }

    @Test(expected = IOException.class)
    public void invalidDirectory() throws IOException {
        fileManager.withDirectory(Path.testJava("ExampleTest").getAbsolutePath());
    }

    @Test
    public void classFile() throws IOException {
        assertTrue(new FileManager().acceptClassFile(Path.clazz("ModelAnalyzer")));
    }

    @Test
    public void nonExistentClassFile() {
        assertFalse(new FileManager().acceptClassFile(Path.testClass("JDepend")));
    }

    @Test
    public void invalidClassFile() {
        assertFalse(new FileManager().acceptClassFile(Path.testJava("ExampleTest")));
    }

    @Test
    public void jar() throws IOException {
        File f = File.createTempFile("bogus", ".jar", Path.testResource(""));
        fileManager.withDirectory(f.getPath());
        f.deleteOnExit();
    }

    @Test
    public void zip() throws IOException {
        File f = File.createTempFile("bogus", ".zip", Path.testResource(""));
        fileManager.withDirectory(f.getPath());
        f.deleteOnExit();
    }

    @Test
    public void war() throws IOException {
        File f = File.createTempFile("bogus", ".war", Path.testResource(""));
        fileManager.withDirectory(f.getPath());
        f.deleteOnExit();
    }
}