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
package guru.nidi.codeassert.dependency;

/**
 * Define the dependency rules for a package and sub packages thereof.
 * A typical example looks as follows:
 * <pre>
 *     class OrgMyProject implements DependencyRuler {
 *         DependencyRule util, core_
 *
 *         public void defineRules() {
 *             base().mayUse(util, core_);
 *             util.mustNotUse(base(), core_);
 *         }
 *     }
 * </pre>
 * This class defines dependency rules for the package org.my.project.
 * All fields of type JavaElement or DependencyRule refer to one or multiple packages:
 * <table summary="">
 * <tr>
 * <th>Field</th>
 * <th>Package</th>
 * </tr>
 * <tr>
 * <td>base()</td>
 * <td>org.my.project</td>
 * </tr>
 * <tr>
 * <td>util</td>
 * <td>org.my.project.util</td>
 * </tr>
 * <tr>
 * <td>core_</td>
 * <td>org.my.project.core and all sub packages</td>
 * </tr>
 * </table>
 * The method #defineRules defines upon which packages a package may, must or must not depend.
 */
public class DependencyRuler {
    DependencyRule unnamed;
    DependencyRule base;
    DependencyRule all;

    /**
     * The unnamed package (when no package statement is used).
     *
     * @return the unnamed package.
     */
    public DependencyRule unnamed() {
        return DependencyRules.addRuleToCurrent(unnamed);
    }

    /**
     * The package this DependencyRuler is defined for.
     *
     * @return the DependencyRuler's package.
     */
    public DependencyRule base() {
        return DependencyRules.addRuleToCurrent(base);
    }

    /**
     * All packages inside this DependencyRuler.
     *
     * @return all DependencyRuler's subpackages.
     */
    public DependencyRule all() {
        return DependencyRules.addRuleToCurrent(all);
    }

    /**
     * An empty rule, can be used to initialize DependencyRule fields.
     *
     * @return an empty rule.
     */
    public DependencyRule rule() {
        return new DependencyRule("*", true);
    }

    /**
     * Create a rule for a package allowing everything.
     * Shortcut for DependencyRule.allowAll(name);
     *
     * @param name the package name
     * @return the rule.
     */
    public DependencyRule allowRule(String name) {
        return new DependencyRule(name, true);
    }

    /**
     * Create a rule for a package denying everything.
     * Shortcut for DependencyRule.denyAll(name);
     *
     * @param name the package name
     * @return the rule.
     */
    public DependencyRule denyRule(String name) {
        return new DependencyRule(name, false);
    }

    public void defineRules() {
    }
}
