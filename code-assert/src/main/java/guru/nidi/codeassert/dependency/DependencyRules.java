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


import guru.nidi.codeassert.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class DependencyRules {
    private static final Logger LOG = LoggerFactory.getLogger(DependencyRules.class);
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile(".*?\\$\\d+");
    private static final ThreadLocal<DependencyRules> CURRENT = new ThreadLocal<>();

    private final List<DependencyRule> rules;
    private final boolean allowAll;
    private final boolean allowIntraPackageCycles;
    final boolean allowIntraPackageDeps;

    private DependencyRules(List<DependencyRule> rules, boolean allowAll,
                            boolean allowIntraPackageCycles, boolean allowIntraPackageDeps) {
        this.rules = rules;
        this.allowAll = allowAll;
        this.allowIntraPackageCycles = allowIntraPackageCycles;
        this.allowIntraPackageDeps = allowIntraPackageDeps;
    }

    public static DependencyRules allowAll() {
        return new DependencyRules(new ArrayList<DependencyRule>(), true, true, true);
    }

    public static DependencyRules denyAll() {
        return new DependencyRules(new ArrayList<DependencyRule>(), false, false, false);
    }

    public DependencyRules allowIntraPackageCycles(boolean allowIntraPackageCycles) {
        return new DependencyRules(rules, allowAll, allowIntraPackageCycles, allowIntraPackageDeps);
    }

    public DependencyRules allowIntraPackageDependencies(boolean allowIntraPackageDeps) {
        return new DependencyRules(rules, allowAll, allowIntraPackageCycles, allowIntraPackageDeps);
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
        final DependencyRule rule = rule(pack).optional();
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
            ruler.unnamed = rule(Model.UNNAMED_PACKAGE);
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
            if (f.getType() == CodeElement.class) {
                ruleFields.add(initField(basePackage, ruler, f));
            }
            if (f.getType() == DependencyRule.class) {
                ruleFields.add(addRule(initField(basePackage, ruler, f)));
            }
        }
        return ruleFields;
    }

    private DependencyRule initField(String basePackage, DependencyRuler ruler, Field f) throws IllegalAccessException {
        final CodeElement value = (CodeElement) f.get(ruler);
        if (value instanceof DependencyRule && !"*".equals(value.pattern.getPattern())) {
            return (DependencyRule) value;
        }
        final String name = f.getName();
        deprecationWarnings(name);
        final String pack = addPackages(basePackage, "$self".equals(name) ? "" : camelCaseToDotCase(name));
        final DependencyRule rule = rule(pack);
        f.set(ruler, rule);
        return rule;
    }

    private void deprecationWarnings(String name) {
        if ("$self".equals(name)) {
            LOG.warn("'DependencyRule $self': $self is deprecated. Use base() instead.");
        }
        if ("_".equals(name)) {
            LOG.warn("'DependencyRule _': _ is deprecated. Use all() instead.");
        }
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
        if (clazz == null) {
            return addPackages(base, "");
        }
        if (isAnonymous(clazz)) {
            return addPackages(base.length() > 0 ? base : clazz.getPackage().getName(), "");
        }
        return addPackages(base, camelCaseToDotCase(reallySimpleName(clazz)));
    }

    private boolean isAnonymous(Class<?> clazz) {
        //anonymous local classes return false to isAnonymousClass()
        return clazz.isAnonymousClass() || ANONYMOUS_CLASS.matcher(clazz.getSimpleName()).matches();
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

    public DependencyRule rule(String pattern) {
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

    public <T extends UsingElement<T>> Dependencies analyzeRules(Scope<T> scope) {
        final Dependencies result = new Dependencies();
        for (final DependencyRule rule : rules) {
            result.merge(rule.analyzer(scope, this).analyze());
        }
        for (final T elem : scope) {
            if (!elem.matchesAny(rules)) {
                result.undefined.add(elem.getName());
            }
        }
        result.normalize();
        result.cycles.addAll(new Tarjan<T>().analyzeCycles(scope, allowIntraPackageCycles));
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
}
