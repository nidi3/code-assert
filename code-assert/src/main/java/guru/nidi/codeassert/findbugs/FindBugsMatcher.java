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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.*;
import guru.nidi.codeassert.config.AnalyzerConfig;
import guru.nidi.codeassert.util.ResultMatcher;
import org.hamcrest.Description;

import java.io.File;
import java.util.List;

public class FindBugsMatcher extends ResultMatcher<FindBugsResult, BugInstance> {
    public void describeTo(Description description) {
        description.appendText("Has no FindBugs issues");
    }

    @Override
    protected void describeMismatchSafely(FindBugsResult item, Description description) {
        for (final BugInstance bug : item.findings()) {
            description.appendText("\n")
                    .appendText(printBug(bug, ((FindBugsAnalyzer) item.analyzer()).config.getSourcePaths()));
        }
    }

    private String printBug(BugInstance bug, List<AnalyzerConfig.Path> sources) {
        final int rank = BugRanker.findRank(bug);
        final SourceLineAnnotation line = bug.getPrimarySourceLineAnnotation();
        final int startLine = line.getStartLine() <= 0 ? 0 : line.getStartLine();
        final String msg = bug.getMessage();
        final int pos = msg.indexOf(':');
        final String message = msg.substring(pos + 2).replace('\n', ' ');
        return String.format("%2d %-8s %-45s %s:%d    %s",
                rank, priority(bug), bug.getType(), sourcePath(line.getSourcePath(), sources), startLine, message);
    }

    private String sourcePath(String sourcePath, List<AnalyzerConfig.Path> sources) {
        for (final AnalyzerConfig.Path source : sources) {
            final File file = new File(source.getBase(), sourcePath);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return sourcePath;
    }

    private String priority(BugInstance bug) {
        switch (bug.getPriority()) {
            case Priorities.EXP_PRIORITY:
                return "E";
            case Priorities.LOW_PRIORITY:
                return "L";
            case Priorities.NORMAL_PRIORITY:
                return "M";
            case Priorities.HIGH_PRIORITY:
                return "H";
            default:
                return "?";
        }
    }
}
