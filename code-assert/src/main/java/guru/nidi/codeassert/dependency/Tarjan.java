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

import guru.nidi.codeassert.model.UsingElement;

import java.util.*;

class Tarjan<T extends UsingElement<T>> {
    private int index;
    private final Stack<T> stack = new Stack<>();
    private final Map<String, Node> nodes = new HashMap<>();
    private final CycleResult result = new CycleResult();

    private static class Node {
        int index = -1;
        int lowlink;
        boolean onStack;
    }

    public CycleResult analyzeCycles(Iterable<T> elems) {
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
        stack.push(elem);
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
            w = stack.pop();
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
                    g.with(0, elem, dep);
                }
            }
        }
        result.cycles.add(g);
    }
}
