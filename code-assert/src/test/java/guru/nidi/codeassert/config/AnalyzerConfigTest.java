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
package guru.nidi.codeassert.config;

import guru.nidi.codeassert.config.AnalyzerConfig.Path;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzerConfigTest {
    @Test
    void withMethods() {
        final AnalyzerConfig config = new AnalyzerConfig()
                .withSources(new File("empty/path"))
                .withSources(new File("/etc"), "c", "/d")
                .withClasses(new File("/tmp"), "a", "/b");

        assertPath(config.getSourcePaths(), path("empty/path", ""), path("/etc", "/c"), path("/etc/", "d"));
        assertPath(config.getClassPaths(), path("/tmp", "/a"), path("/tmp/", "b"));
    }

    @Test
    void mavenSimple() {
        final AnalyzerConfig config = AnalyzerConfig.maven().main();
        assertPath(config.getSourcePaths(), path("src/main/java", ""));
        assertPath(config.getClassPaths(), path("target/classes", ""));
    }

    @Test
    void mavenModule() {
        final AnalyzerConfig config = AnalyzerConfig.maven("module").main();
        assertPath(config.getSourcePaths(), path("module/src/main/java", ""));
        assertPath(config.getClassPaths(), path("module/target/classes", ""));
    }

    @Test
    void mavenOwnModule() {
        final AnalyzerConfig config = AnalyzerConfig.maven("code-assert").mainAndTest();
        assertPath(config.getSourcePaths(), path("src/main/java", ""), path("src/test/java", ""));
        assertPath(config.getClassPaths(), path("target/classes", ""), path("target/test-classes", ""));
    }

    @Test
    void mavenPackages() {
        final AnalyzerConfig config = AnalyzerConfig.maven().test("mypack");
        assertPath(config.getSourcePaths(), path("src/test/java", "mypack"));
        assertPath(config.getClassPaths(), path("target/test-classes", "mypack"));
    }



    @Test
    void simplePath() {
        final Path path = new Path("a", "b");
        assertEquals("a", path.getBase());
        assertEquals("b", path.getPack());
        assertEquals("a/b", path.getPath());
    }

    @Test
    void slashPath() {
        final Path path = new Path("/a/", "/b/");
        assertEquals("/a", path.getBase());
        assertEquals("b/", path.getPack());
        assertEquals("/a/b/", path.getPath());
    }

    @Test
    void emptyPack() {
        final Path path = new Path("/a/", "");
        assertEquals("/a", path.getBase());
        assertEquals("", path.getPack());
        assertEquals("/a", path.getPath());
    }

    private Path path(String base, String pack) {
        return new Path(base, pack);
    }

    private void assertPath(List<Path> paths, Path... expected) {
        assertEquals(Arrays.asList(expected), paths);
    }
}
