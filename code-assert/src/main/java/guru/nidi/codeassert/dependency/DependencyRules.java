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


import guru.nidi.codeassert.model.Model;
import guru.nidi.codeassert.model.UsingElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class DependencyRules {
    private final List<DependencyRule> rules = new ArrayList<>();
    private final boolean allowAll;
    private static final ThreadLocal<DependencyRules> CURRENT = new ThreadLocal<>();

    private DependencyRules(boolean allowAll) {
        this.allowAll = allowAll;
    }

    public static DependencyRules allowAll() {
        return new DependencyRules(true);
    }

    public static DependencyRules denyAll() {
        return new DependencyRules(false);
    }

    public DependencyRule addRule(String pack) {
        return addRule(rule(pack));
    }

    public DependencyRule addRule(DependencyRule pack) {
        rules.add(pack);
        return pack;
    }

    /**
     * An external package has a default {@code mayBeUsedBy("*")}.
     *
     * @param pack The name of the external package.
     * @return A DependencyRule with the given name.
     */
    public DependencyRule addExternal(String pack) {
        final DependencyRule rule = rule(pack);
        rule.mayBeUsedBy(rule("*"));
        return addRule(rule);
    }

    /**
     * Add rules defined by a DependencyRuler class.
     * The following DependencyRules are all equal:
     * <pre>
     * DependencyRules rules1 = DependencyRules.allowAll();
     * DependencyRule a = rules1.addRule("com.acme.a.*"));
     * DependencyRule b = rules1.addRule("com.acme.sub.b"));
     * a.mustNotUse(b);
     * </pre>
     * ----
     * <pre>
     * class ComAcme implements DependencyRuler{
     *     DependencyRule a_, subB;
     *
     *     public defineRules(){
     *         a_.mustNotUse(subB);
     *     }
     * }
     * DependencyRules rules2 = DependencyRules.allowAll().addRules(new ComAcme());
     * </pre>
     * ----
     * <pre>
     * DependencyRules rules3 = DependencyRules.allowAll().addRules("com.acme", new DependencyRuler(){
     *     DependencyRule a_, subB;
     *
     *     public defineRules(){
     *         a_.mustNotUse(subB);
     *     }
     * });
     * </pre>
     *
     * @param basePackage the package to be prepended in front of the ruler's name
     * @param ruler       defines the dependency rules
     * @return DependencyRules including the new rules.
     */
    public DependencyRules withRules(String basePackage, DependencyRuler ruler) {
        return doWithRules(addPackages(basePackage, ruler.getClass()), false, ruler);
    }

    public DependencyRules withAbsoluteRules(DependencyRuler... rulers) {
        return doWithRules(false, false, rulers);
    }

    public DependencyRules withRelativeRules(DependencyRuler... rulers) {
        return doWithRules(true, false, rulers);
    }

    public DependencyRules withExternals(DependencyRuler... rulers) {
        return doWithRules(false, true, rulers);
    }

    public DependencyRules withExternals(String... externals) {
        for (final String external : externals) {
            addExternal(external);
        }
        return this;
    }

    private DependencyRules doWithRules(boolean withRulerName, boolean external, DependencyRuler... rulers) {
        for (final DependencyRuler ruler : rulers) {
            doWithRules(addPackages("", withRulerName ? ruler.getClass() : null), external, ruler);
        }
        return this;
    }

    private DependencyRules doWithRules(String basePackage, boolean external, DependencyRuler ruler) {
        CURRENT.set(this);
        try {
            final List<DependencyRule> ruleFields = initFields(basePackage, ruler);
            if (basePackage.length() > 0) {
                ruler.base = rule(addPackages(basePackage, ""));
            }
            ruler.all = rule(addPackages(basePackage, "*"));
            ruler.defineRules();
            postProcessFields(ruleFields, external);
            return this;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access field", e);
        } finally {
            CURRENT.remove();
        }
    }

    static DependencyRule addRuleToCurrent(DependencyRule rule) {
        final DependencyRules rules = DependencyRules.CURRENT.get();
        if (rules != null) {
            rules.addRule(rule);
        }
        return rule;
    }

    private List<DependencyRule> initFields(String basePackage, DependencyRuler ruler) throws IllegalAccessException {
        final List<DependencyRule> ruleFields = new ArrayList<>();
        for (final Field f : ruler.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType() == DependencyRule.class) {
                final String pack = addPackages(basePackage,
                        f.getName().equals("$self") ? "" : camelCaseToDotCase(f.getName()));
                final DependencyRule rule = addRule(pack);
                ruleFields.add(rule);
                f.set(ruler, rule);
            }
        }
        return ruleFields;
    }

    private void postProcessFields(List<DependencyRule> ruleFields, boolean external) {
        if (external) {
            for (final DependencyRule rule : ruleFields) {
                if (rule.isEmpty()) {
                    rule.mayBeUsedBy(rule("*"));
                }
            }
        }
    }

    private String addPackages(String base, Class<?> clazz) {
        final String name = clazz == null || clazz.isAnonymousClass()
                ? ""
                : camelCaseToDotCase(reallySimpleName(clazz));
        return addPackages(base, name);
    }

    private String addPackages(String p1, String p2) {
        return p1.length() > 0 && !p1.endsWith(".") && p2.length() > 0
                ? p1 + "." + p2
                : p1 + p2;
    }

    private String reallySimpleName(Class<?> clazz) {
        final String simple = clazz.getSimpleName();
        final String prefix = clazz.getEnclosingMethod() == null ? "" : clazz.getEnclosingMethod().getName() + "$";
        return simple.startsWith(prefix) ? simple.substring(prefix.length()) : simple;
    }

    private static String camelCaseToDotCase(String s) {
        final StringBuilder res = new StringBuilder();
        final boolean dollarMode = s.contains("$");
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '_' && i == s.length() - 1) {
                res.append(res.length() == 0 || res.charAt(res.length() - 1) == '.' ? "*" : ".*");
            } else {
                res.append(processChar(dollarMode, i == 0, c));
            }
        }
        return res.toString();
    }

    private DependencyRule rule(String pattern) {
        return new DependencyRule(pattern, allowAll);
    }

    private static String processChar(boolean dollarMode, boolean firstChar, char c) {
        if (dollarMode) {
            if (c == '$' && !firstChar) {
                return ".";
            }
            return Character.toString(c);
        }
        if (Character.isUpperCase(c)) {
            return (firstChar ? "" : ".") + Character.toLowerCase(c);
        }
        return Character.toString(c);
    }

    public <T extends UsingElement<T>> RuleResult analyzeRules(Model.View<T> view) {
        final RuleResult result = new RuleResult();
        for (final DependencyRule rule : rules) {
            result.merge(rule.analyzer(view, this).analyze());
        }
        for (final T elem : view) {
            if (!elem.matchesAny(rules)) {
                result.undefined.add(elem.getName());
            }
        }
        result.normalize();
        return result;
    }

    <T extends UsingElement<T>> int mostSpecificUsageMatch(T from, T to, RuleAccessor accessor) {
        int s = 0;
        for (final DependencyRule rule : rules) {
            if (rule.matches(to)) {
                s = Math.max(s, from.mostSpecificMatch(accessor.access(rule)));
            }
        }
        return s;
    }

    public static <T extends UsingElement<T>> CycleResult analyzeCycles(Model.View<T> view) {
        return new Tarjan<T>().analyzeCycles(view);
    }
}
