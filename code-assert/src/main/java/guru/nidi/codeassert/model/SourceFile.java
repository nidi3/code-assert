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

import java.util.ArrayList;
import java.util.List;

public class SourceFile {
    final String name;
    final List<CodeClass> classes = new ArrayList<>();
    final int codeLines;
    final int commentLines;
    final int emptyLines;
    final int totalLines;

    SourceFile(String name, int codeLines, int commentLines, int emptyLines, int totalLines) {
        this.name = name;
        this.codeLines = codeLines;
        this.commentLines = commentLines;
        this.emptyLines = emptyLines;
        this.totalLines = totalLines;
    }

    public String getName() {
        return name;
    }

    public List<CodeClass> getClasses() {
        return classes;
    }

    public int getCodeLines() {
        return codeLines;
    }

    public int getCommentLines() {
        return commentLines;
    }

    public int getEmptyLines() {
        return emptyLines;
    }

    public int getTotalLines() {
        return totalLines;
    }
}
