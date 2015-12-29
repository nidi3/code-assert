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

import java.lang.reflect.Field;

/**
 *
 */
public class Ruleset {
    public final String name;

    public Ruleset(String name) {
        this.name = name;
    }

    public void apply(PMDConfiguration config) {
        for (final Field descField : getClass().getDeclaredFields()) {
            try {
                descField.setAccessible(true);
                final Object value = descField.get(this);
                if (value != null) {
                    if (RuleDescriptor.class.isAssignableFrom(descField.getType())) {
                        ((RuleDescriptor) value).apply(config, descField.getType().getSimpleName());
                    } else {
                        final PropertyField propertyField = descField.getAnnotation(PropertyField.class);
                        if (propertyField != null) {
                            RuleDescriptor.setProperty(config, propertyField.rule(), propertyField.value(), value);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not read property " + descField.getName() + " from class " + getClass(), e);
            }
        }
    }
}
