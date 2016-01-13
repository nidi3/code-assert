/*
 * Copyright (C) 2015 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.codeassert.dependency;


import guru.nidi.codeassert.model.JavaPackage;
import guru.nidi.codeassert.model.Model;
import guru.nidi.codeassert.model.UsingElement;

import java.lang.reflect.Field;
import java.util.*;

/**
 *
 */
public class DependencyRules {
    private final List<DependencyRule> rules = new ArrayList<>();
    private final boolean allowAll;

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
        return addRule(new DependencyRule(pack, allowAll));
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
        final DependencyRule rule = new DependencyRule(pack, allowAll);
        rule.mayBeUsedBy(new DependencyRule("*", allowAll));
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

    public DependencyRules withRules(DependencyRuler... rulers) {
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
        try {
            final List<DependencyRule> ruleFields = initFields(basePackage, ruler);
            ruler.defineRules();
            postProcessFields(ruleFields, external);
            return this;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access field", e);
        }
    }

    private List<DependencyRule> initFields(String basePackage, DependencyRuler ruler) throws IllegalAccessException {
        final List<DependencyRule> ruleFields = new ArrayList<>();
        for (final Field f : ruler.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType() == DependencyRule.class) {
                final String pack = addPackages(basePackage, (f.getName().equals("$self") ? "" : camelCaseToDotCase(f.getName())));
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
                    rule.mayBeUsedBy(new DependencyRule("*", allowAll));
                }
            }
        }
    }

    private String addPackages(String base, Class<?> clazz) {
        final String name = clazz == null || clazz.isAnonymousClass()
                ? ""
                : camelCaseToDotCase(clazz.getSimpleName());
        return addPackages(base, name);
    }

    private String addPackages(String p1, String p2) {
        return p1.length() > 0 && !p1.endsWith(".") && p2.length() > 0
                ? p1 + "." + p2
                : p1 + p2;
    }

    private static String camelCaseToDotCase(String s) {
        final StringBuilder res = new StringBuilder();
        final boolean dollarMode = s.contains("$");
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '_' && i == s.length() - 1) {
                res.append(res.charAt(res.length() - 1) == '.' ? "*" : ".*");
            } else {
                res.append(processChar(dollarMode, i == 0, c));
            }
        }
        return res.toString();
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

    public RuleResult analyzeRules(Model model) {
        final RuleResult result = new RuleResult();
        for (final DependencyRule rule : rules) {
            result.merge(rule.analyze(model, rules));
        }
        for (final JavaPackage pack : model.getPackages()) {
            if (!matchesAny(pack, rules)) {
                result.undefined.add(pack.getName());
            }
        }
        result.normalize();
        return result;
    }

    private boolean matchesAny(JavaPackage pack, List<DependencyRule> rules) {
        for (final DependencyRule rule : rules) {
            if (rule.matches(pack)) {
                return true;
            }
        }
        return false;
    }

    public static <T extends UsingElement<T>> CycleResult analyzeCycles(Collection<T> elems) {
        return new Tarjan<T>().analyzeCycles(elems);
    }

    private static class Tarjan<T extends UsingElement<T>> {
        private int index;
        private final Stack<T> s = new Stack<>();
        private final Map<String, Node> nodes = new HashMap<>();
        private final CycleResult result = new CycleResult();

        private static class Node {
            int index = -1;
            int lowlink;
            boolean onStack;
        }

        public CycleResult analyzeCycles(Collection<T> elems) {
            index = 0;
            for (final T elem : elems) {
                if (node(elem).index < 0) {
                    strongConnect(elem);
                }
            }
            return result;
        }

        private Node node(T elem) {
            Node node = nodes.get(elem.getName());
            if (node == null) {
                node = new Node();
                nodes.put(elem.getName(), node);
            }
            return node;
        }

        private void strongConnect(T elem) {
            final Node v = init(elem);
            processUses(elem, v);

            if (v.lowlink == v.index) {
                final Set<T> group = createGroup(elem);
                if (group.size() > 1) {
                    addCycle(group);
                }
            }
        }

        private Node init(T elem) {
            final Node v = node(elem);
            v.index = index;
            v.lowlink = index;
            index++;
            s.push(elem);
            v.onStack = true;
            return v;
        }

        private void processUses(T elem, Node v) {
            for (final T dep : elem.uses()) {
                final Node w = node(dep);
                if (w.index < 0) {
                    strongConnect(dep);
                    v.lowlink = Math.min(v.lowlink, w.lowlink);
                } else if (w.onStack) {
                    v.lowlink = Math.min(v.lowlink, w.index);
                }
            }
        }

        private Set<T> createGroup(T elem) {
            final Set<T> group = new HashSet<>();
            T w;
            do {
                w = s.pop();
                node(w).onStack = false;
                group.add(w);
            } while (!elem.equals(w));
            return group;
        }

        private void addCycle(Set<T> group) {
            final DependencyMap g = new DependencyMap();
            for (final T elem : group) {
                for (final T dep : elem.uses()) {
                    if (group.contains(dep)) {
                        g.with(elem, dep);
                    }
                }
            }
            result.cycles.add(g);
        }
    }
}