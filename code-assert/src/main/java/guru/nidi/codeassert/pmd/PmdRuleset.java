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
package guru.nidi.codeassert.pmd;

import guru.nidi.codeassert.AnalyzerException;
import net.sourceforge.pmd.*;

import java.lang.reflect.Field;

public class PmdRuleset {
    final String name;

    public PmdRuleset(String name) {
        this.name = name;
    }

    public void apply(PMDConfiguration config) {
        for (final Field descField : getClass().getDeclaredFields()) {
            final PropertyField propertyField = descField.getAnnotation(PropertyField.class);
            if (propertyField != null) {
                try {
                    descField.setAccessible(true);
                    final Object value = descField.get(this);
                    if (value != null) {
                        setProperty(config, propertyField.rule(), propertyField.property(), value);
                    }
                } catch (IllegalAccessException e) {
                    throw new AnalyzerException("Could not read property " + descField.getName()
                            + " from class " + getClass(), e);
                }
            }
        }
    }

    private void setProperty(PMDConfiguration config, String rule, String property, Object value) {
        final Rule r = config.getPmdRuleSets().getRuleByName(rule);
        if (r == null) {
            throw new AnalyzerException("Rule '" + rule + "' not existing.");
        }
        final PropertyDescriptor<Object> descriptor = (PropertyDescriptor<Object>) r.getPropertyDescriptor(property);
        if (descriptor == null) {
            throw new AnalyzerException("Property '" + property + "' for rule '" + rule + "' not existing.");
        }
        r.setProperty(descriptor, value);
    }
}
