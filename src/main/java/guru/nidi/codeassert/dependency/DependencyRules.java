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
    private final List<PackageRule> rules = new ArrayList<>();
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

    public PackageRule addRule(String pack) {
        final PackageRule rule = new PackageRule(pack, allowAll);
        rules.add(rule);
        return rule;
    }

    public PackageRule addRule(PackageRule pack) {
        rules.add(pack);
        return pack;
    }

    /**
     * Add rules defined by a RuleDefiner class. The following DependencyRules are all equal:
     * <pre>
     * DependencyRules rules1 = DependencyRules.allowAll();
     * PackageRule a = rules1.addRule("com.acme.a.*"));
     * PackageRule b = rules1.addRule("com.acme.sub.b"));
     * a.mustNotDependUpon(b);
     * </pre>
     * ----
     * <pre>
     * class ComAcme implements RuleDefiner{
     *     PackageRule a_, subB;
     *
     *     public defineRules(){
     *         a_.mustNotDependUpon(subB);
     *     }
     * }
     * DependencyRules rules2 = DependencyRules.allowAll().addRules(new ComAcme());
     * </pre>
     * ----
     * <pre>
     * DependencyRules rules3 = DependencyRules.allowAll().addRules("com.acme", new RuleDefiner(){
     *     PackageRule a_, subB;
     *
     *     public defineRules(){
     *         a_.mustNotDependUpon(subB);
     *     }
     * });
     * </pre>
     *
     * @param basePackage
     * @param definer
     * @return
     */
    public DependencyRules withRules(String basePackage, RuleDefiner definer) {
        try {
            for (Field f : definer.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == PackageRule.class) {
                    final String name = definer.getClass().isAnonymousClass()
                            ? ""
                            : camelCaseToDotCase(definer.getClass().getSimpleName());
                    final String start = basePackage.length() > 0 && !basePackage.endsWith(".") && name.length() > 0
                            ? basePackage + "." + name
                            : basePackage + name;
                    f.set(definer, addRule(start + (f.getName().equals("self") ? "" : ("." + camelCaseToDotCase(f.getName())))));
                }
            }
            definer.defineRules();
            return this;
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access field", e);
        }
    }

    public DependencyRules withRules(RuleDefiner... definers) {
        for (final RuleDefiner definer : definers) {
            withRules("", definer);
        }
        return this;
    }

    private static String camelCaseToDotCase(String s) {
        final StringBuilder res = new StringBuilder();
        final boolean dollarMode = s.contains("$");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '_' && i == s.length() - 1) {
                res.append(res.charAt(res.length() - 1) == '.' ? "*" : ".*");
            } else {
                if (dollarMode) {
                    if (c == '$' && i > 0) {
                        res.append(".");
                    } else {
                        res.append(c);
                    }
                } else {
                    if (Character.isUpperCase(c)) {
                        if (i > 0) {
                            res.append(".");
                        }
                        res.append(Character.toLowerCase(c));
                    } else {
                        res.append(c);
                    }
                }
            }
        }
        return res.toString();
    }

    public RuleResult analyzeRules(Collection<JavaPackage> packs) {
        final RuleResult result = new RuleResult();
        for (final PackageRule rule : rules) {
            result.merge(rule.analyze(packs));
        }
        for (final JavaPackage pack : packs) {
            boolean defined = false;
            for (final PackageRule rule : rules) {
                if (rule.matches(pack)) {
                    defined = true;
                    break;
                }
            }
            if (!defined) {
                result.undefined.add(pack.getName());
            }
        }
        result.normalize();
        return result;
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
            for (JavaPackage pack : packs) {
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
            final Node v = node(pack);
            v.index = index;
            v.lowlink = index;
            index++;
            s.push(pack);
            v.onStack = true;
            for (JavaPackage dep : pack.getEfferents()) {
                final Node w = node(dep);
                if (w.index < 0) {
                    strongConnect(dep);
                    v.lowlink = Math.min(v.lowlink, w.lowlink);
                } else if (w.onStack) {
                    v.lowlink = Math.min(v.lowlink, w.index);
                }
            }

            if (v.lowlink == v.index) {
                final Set<JavaPackage> group = new HashSet<>();
                JavaPackage w;
                do {
                    w = s.pop();
                    node(w).onStack = false;
                    group.add(w);
                } while (!pack.equals(w));
                if (group.size() > 1) {
                    final DependencyMap g = new DependencyMap();
                    for (JavaPackage elem : group) {
                        for (JavaPackage dep : elem.getEfferents()) {
                            if (group.contains(dep)) {
                                g.with(elem, dep);
                            }
                        }
                    }
                    result.cycles.add(g);
                }
            }
        }
    }
}