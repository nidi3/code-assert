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
package guru.nidi.codeassert;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.*;

import static guru.nidi.codeassert.AssertMojo.JACOCO_VERSION;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Prepare the maven surefire plugin to run the tests with a Jacoco Agent.
 */
@Mojo(name = "prepare", defaultPhase = LifecyclePhase.INITIALIZE)
public class PrepareMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    public void execute() throws MojoExecutionException, MojoFailureException {
        executeJacocoPlugin();
        writeArgLineFile();
    }

    private void executeJacocoPlugin() throws MojoExecutionException {
        executeMojo(
                plugin(
                        groupId("org.jacoco"),
                        artifactId("jacoco-maven-plugin"),
                        version(JACOCO_VERSION)
                ),
                goal("prepare-agent"),
                configuration(),
                executionEnvironment(mavenProject, mavenSession, pluginManager)
        );
    }

    private void writeArgLineFile() {
        final String argLine = mavenProject.getProperties().getProperty("argLine");
        if (argLine != null) {
            final File file = new File("target", "coverageOptions.txt");
            file.getParentFile().mkdirs();
            try (final Writer out = new OutputStreamWriter(new FileOutputStream(file), "utf-8")) {
                out.write(argLine);
                getLog().info("Wrote argLine to " + file);
            } catch (IOException e) {
                getLog().warn("Could not write to " + file, e);
            }
        }
    }
}