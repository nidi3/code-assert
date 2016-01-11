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

import java.lang.reflect.Field;
import java.util.*;

/**
 *
 */
public class DependencyRules {
    private final List<DependencyRule> rules = new ArrayList<>();
    private final List<DependencyRule> externals = new ArrayList<>();
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
        return doAddRule(pack, rules);
    }

    public DependencyRule addRule(DependencyRule pack) {
        rules.add(pack);
        return pack;
    }

    /**
     * An external package and usages thereof are ignored.
     *
     * @param pack
     * @return
     */
    public DependencyRule addExternal(String pack) {
        return doAddRule(pack, externals);
    }

    /**
     * The package of an external rule is ignored,
     * it must not contain "use" clauses, but its "beUsedBy" clauses are applied.
     *
     * @param pack
     * @return
     */
    public DependencyRule addExternal(DependencyRule pack) {
        externals.add(pack);
        return pack;
    }

    private DependencyRule doAddRule(String pack, List<DependencyRule> rules) {
        final DependencyRule rule = new DependencyRule(pack, allowAll);
        rules.add(rule);
        return rule;
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
        return doWithRules(addPackages(basePackage, ruler.getClass()), ruler, rules);
    }


    public DependencyRules withRules(DependencyRuler... rulers) {
        return doWithRules(rules, true, rulers);
    }

    public DependencyRules withExternals(DependencyRuler... rulers) {
        return doWithRules(externals, false, rulers);
    }

    public DependencyRules withExternals(String... externals) {
        for (final String external : externals) {
            addExternal(external);
        }
        return this;
    }

    private DependencyRules doWithRules(List<DependencyRule> rules, boolean withRulerName, DependencyRuler... rulers) {
        for (final DependencyRuler ruler : rulers) {
            doWithRules(addPackages("", withRulerName ? ruler.getClass() : null), ruler, rules);
        }
        return this;
    }

    private DependencyRules doWithRules(String basePackage, DependencyRuler ruler, List<DependencyRule> rules) {
        try {
            for (final Field f : ruler.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == DependencyRule.class) {
                    final String pack = addPackages(basePackage, (f.getName().equals("$self") ? "" : camelCaseToDotCase(f.getName())));
                    f.set(ruler, doAddRule(pack, rules));
                }
            }
            ruler.defineRules();
            return this;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access field", e);
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

    public RuleResult analyzeRules(Collection<JavaPackage> packs) {
        checkRules();
        final RuleResult result = new RuleResult();
        final Collection<JavaPackage> filtered = filterExternals(packs);
        for (final DependencyRule rule : rules) {
            result.merge(rule.analyze(filtered, rules));
        }
        for (final JavaPackage pack : filtered) {
            if (!matchesAny(pack, rules)) {
                result.undefined.add(pack.getName());
            }
        }
        result.normalize();
        return result;
    }

    private Collection<JavaPackage> filterExternals(Collection<JavaPackage> packs) {
        final List<JavaPackage> filtered = new ArrayList<>();
        for (final JavaPackage pack : packs) {
            if (!matchesAny(pack, externals)) {
                filtered.add(filterMatchingEfferents(pack, externals));
            }
        }
        return filtered;
    }

    private JavaPackage filterMatchingEfferents(JavaPackage pack, List<DependencyRule> rules) {
        final List<JavaPackage> filtered = new ArrayList<>(pack.getEfferents());
        for (final DependencyRule rule : rules) {
            for (final Iterator<JavaPackage> it = filtered.iterator(); it.hasNext(); ) {
                final JavaPackage eff = it.next();
                if (rule.matches(eff)) {
                    it.remove();
                }
            }
        }
        return pack.copyWithEfferents(filtered);
    }


    private void checkRules() {
        final List<DependencyRule> rs = new ArrayList<>();
        for (final DependencyRule external : externals) {
            if (external.hasUseClause()) {
                rs.add(external);
            }
        }
        if (!rs.isEmpty()) {
            throw new ExternalDependencyWithUseClauseException(rs);
        }
    }

    private boolean matchesAny(JavaPackage pack, List<DependencyRule> rules) {
        for (final DependencyRule rule : rules) {
            if (rule.matches(pack)) {
                return true;
            }
        }
        return false;
    }

    public static CycleResult analyzeCycles(Collection<JavaPackage> packs) {
        return new Tarjan().analyzeCycles(packs);
    }

    private static class Tarjan {
        private int index;
        private final Stack<JavaPackage> s = new Stack<>();
        private final Map<String, Node> nodes = new HashMap<>();
        private final CycleResult result = new CycleResult();

        private static class Node {
            int index = -1;
            int lowlink;
            boolean onStack;
        }

        public CycleResult analyzeCycles(Collection<JavaPackage> packs) {
            index = 0;
            for (final JavaPackage pack : packs) {
                if (node(pack).index < 0) {
                    strongConnect(pack);
                }
            }
            return result;
        }

        private Node node(JavaPackage pack) {
            Node node = nodes.get(pack.getName());
            if (node == null) {
                node = new Node();
                nodes.put(pack.getName(), node);
            }
            return node;
        }

        private void strongConnect(JavaPackage pack) {
            final Node v = init(pack);
            processEfferents(pack, v);

            if (v.lowlink == v.index) {
                final Set<JavaPackage> group = createGroup(pack);
                if (group.size() > 1) {
                    addCycle(group);
                }
            }
        }

        private Node init(JavaPackage pack) {
            final Node v = node(pack);
            v.index = index;
            v.lowlink = index;
            index++;
            s.push(pack);
            v.onStack = true;
            return v;
        }

        private void processEfferents(JavaPackage pack, Node v) {
            for (final JavaPackage dep : pack.getEfferents()) {
                final Node w = node(dep);
                if (w.index < 0) {
                    strongConnect(dep);
                    v.lowlink = Math.min(v.lowlink, w.lowlink);
                } else if (w.onStack) {
                    v.lowlink = Math.min(v.lowlink, w.index);
                }
            }
        }

        private Set<JavaPackage> createGroup(JavaPackage pack) {
            final Set<JavaPackage> group = new HashSet<>();
            JavaPackage w;
            do {
                w = s.pop();
                node(w).onStack = false;
                group.add(w);
            } while (!pack.equals(w));
            return group;
        }

        private void addCycle(Set<JavaPackage> group) {
            final DependencyMap g = new DependencyMap();
            for (final JavaPackage elem : group) {
                for (final JavaPackage dep : elem.getEfferents()) {
                    if (group.contains(dep)) {
                        g.with(elem, dep);
                    }
                }
            }
            result.cycles.add(g);
        }
    }
}