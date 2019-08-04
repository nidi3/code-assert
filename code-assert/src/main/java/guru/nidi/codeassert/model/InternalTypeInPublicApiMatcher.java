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

public class InternalTypeInPublicApiMatcher extends TypeSafeMatcher<Model> {
    private final Map<String, Map<String, List<String>>> findings = new HashMap<>();

    @Override
    protected boolean matchesSafely(Model model) {
        for (final CodeClass clazz : model.getClasses()) {
            if (Modifier.isPublic(clazz.flags)) {
                checkMembers(model, clazz);
            }
        }
        return findings.isEmpty();
    }

    private void checkMembers(Model model, CodeClass clazz) {
        for (final MemberInfo member : clazz.getMembers()) {
            if (Modifier.isPublic(member.getAccessFlags())) {
                for (final String refClassName : member.referencedClasses) {
                    final CodeClass refClass = model.getOrCreateClass(refClassName);
                    if (refClass.isParsed() && !Modifier.isPublic(refClass.flags)) {
                        final Map<String, List<String>> classFindings = findings.computeIfAbsent(clazz.getName(),
                                s -> new HashMap<>());
                        final List<String> memberFindings = classFindings.computeIfAbsent(member.getName(),
                                s -> new ArrayList<>());
                        memberFindings.add(refClass.getName());
                    }
                }
            }
        }
    }

    public void describeTo(Description description) {
        description.appendText("Does not expose internal types in public APIs.");
    }

    @Override
    protected void describeMismatchSafely(Model model, Description description) {
        description.appendText("Found these references to internal members in public APIs:\n");
        for (final Entry<String, Map<String, List<String>>> classFinding : findings.entrySet()) {
            description.appendText("In class " + classFinding.getKey() + "\n");
            for (final Entry<String, List<String>> memberFinding : classFinding.getValue().entrySet()) {
                description.appendText("  " + memberFinding.getKey() + ": " + memberFinding.getValue() + "\n");
            }
        }
    }
}
