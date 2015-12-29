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
package guru.nidi.codeassert.pmd;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.Rule;

import java.lang.reflect.Field;

/**
 *
 */
public class RuleDescriptor<T extends Ruleset> {
    protected final T ruleset;

    public RuleDescriptor(T ruleset) {
        this.ruleset = ruleset;
    }

    public T build() {
        return ruleset;
    }

    public void apply(PMDConfiguration config, String rule) {
        for (final Field prop : getClass().getDeclaredFields()) {
            if (isAllowedPropertyType(prop.getType())) {
                prop.setAccessible(true);
                try {
                    final Object value = prop.get(this);
                    if (value != null) {
                        setProperty(config, rule, prop.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }
    }

    static boolean isAllowedPropertyType(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz.isEnum();
    }

    static void setProperty(PMDConfiguration config, String rule, String property, Object value) {
        final Rule r = config.getPmdRuleSets().getRuleByName(rule);
        final PropertyDescriptor<Object> descriptor = (PropertyDescriptor<Object>) r.getPropertyDescriptor(property);
        r.setProperty(descriptor, value);
    }
}
