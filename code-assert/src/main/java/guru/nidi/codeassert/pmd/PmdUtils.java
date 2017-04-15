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
package guru.nidi.codeassert.pmd;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.cpd.Mark;

final class PmdUtils {
    private PmdUtils() {
    }

    public static String className(Mark mark) {
        return className(null, mark.getFilename());
    }

    public static String className(RuleViolation violation) {
        if (violation.getClassName().length() > 0) {
            return (violation.getPackageName().length() > 0 ? (violation.getPackageName() + ".") : "")
                    + violation.getClassName();
        }
        return className(violation.getPackageName(), violation.getFilename());
    }

    private static String className(String packageName, String filename) {
        if (filename.length() > 0) {
            final int last = filename.lastIndexOf('/');
            String prefix = "";
            if (packageName != null && packageName.length() > 0) {
                prefix = packageName + ".";
            } else {
                //TODO can this heuristic be improved?
                final int src = filename.indexOf("src/") + 3;
                final int java = filename.indexOf("java/") + 4;
                final int later = Math.max(src, java);
                if (later >= 4) {
                    prefix = filename.substring(later + 1, last + 1).replace('/', '.');
                }
            }
            return prefix + filename.substring(last + 1, filename.length() - 5);
        }
        return "?";
    }

}
