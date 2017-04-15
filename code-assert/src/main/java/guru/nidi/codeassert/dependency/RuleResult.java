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

import guru.nidi.codeassert.config.LocationMatcher;

import java.util.Set;
import java.util.TreeSet;

public class RuleResult {
    final DependencyMap allowed;
    final DependencyMap missing;
    final DependencyMap denied;
    final Set<LocationMatcher> notExisting;
    final Set<String> undefined;

    public RuleResult() {
        this(new DependencyMap(), new DependencyMap(), new DependencyMap(),
                new TreeSet<LocationMatcher>(), new TreeSet<String>());
    }

    public RuleResult(DependencyMap allowed, DependencyMap missing, DependencyMap denied,
                      Set<LocationMatcher> notExisting, Set<String> undefined) {
        this.allowed = allowed;
        this.missing = missing;
        this.denied = denied;
        this.notExisting = notExisting;
        this.undefined = undefined;
    }

    public void merge(RuleResult cr) {
        allowed.merge(cr.allowed);
        missing.merge(cr.missing);
        denied.merge(cr.denied);
        notExisting.addAll(cr.notExisting);
        undefined.addAll(cr.undefined);
    }

    // an explicitly allowed dependency is stronger than any denial
    public void normalize() {
        denied.without(allowed);
        allowed.clear();
    }

    public DependencyMap getAllowed() {
        return allowed;
    }

    public DependencyMap getMissing() {
        return missing;
    }

    public DependencyMap getDenied() {
        return denied;
    }

    public Set<LocationMatcher> getNotExisting() {
        return notExisting;
    }

    public Set<String> getUndefined() {
        return undefined;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RuleResult that = (RuleResult) o;

        if (!allowed.equals(that.allowed)) {
            return false;
        }
        if (!missing.equals(that.missing)) {
            return false;
        }
        if (!denied.equals(that.denied)) {
            return false;
        }
        if (!notExisting.equals(that.notExisting)) {
            return false;
        }
        return undefined.equals(that.undefined);
    }

    @Override
    public int hashCode() {
        int result = allowed.hashCode();
        result = 31 * result + missing.hashCode();
        result = 31 * result + denied.hashCode();
        result = 31 * result + notExisting.hashCode();
        result = 31 * result + undefined.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RuleResult{"
                + "allowed=" + allowed
                + ", missing=" + missing
                + ", denied=" + denied
                + ", notExisting=" + notExisting
                + ", undefined=" + undefined
                + '}';
    }
}
