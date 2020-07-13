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

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;

//TODO exclude interface implementing methods
public class PublicMemberInInternalTypeMatcher extends TypeSafeMatcher<Model> {
    private final Map<String, List<String>> findings = new HashMap<>();

    @Override
    protected boolean matchesSafely(Model model) {
        for (final CodeClass clazz : model.getClasses()) {
            if (!Modifier.isPublic(clazz.flags)) {
                checkMembers(clazz);
            }
        }
        return findings.isEmpty();
    }

    private void checkMembers(CodeClass clazz) {
        for (final MemberInfo member : clazz.getMembers()) {
            if (Modifier.isPublic(member.getAccessFlags())) {
                final List<String> classFindings = findings.computeIfAbsent(clazz.getName(), s -> new ArrayList<>());
                classFindings.add(member.getName());
            }
        }
    }

    public void describeTo(Description description) {
        description.appendText("Internal types have no public members.");
    }

    @Override
    protected void describeMismatchSafely(Model model, Description description) {
        description.appendText("Found these public members in internal types:\n");
        for (final Entry<String, List<String>> classFinding : findings.entrySet()) {
            description.appendText(classFinding.getKey() + ": " + classFinding.getValue() + "\n");
        }
    }
}
