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

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.renderers.Renderer;
import org.junit.Test;

/**
 *
 */
public class PmdTest {
    @Test
    public void simple() {
        final PMDConfiguration config = new PMDConfiguration() {
            @Override
            public Renderer createRenderer() {
                setProperty(this, "TooManyMethods", "maxmethods", "25");
                return super.createRenderer();
            }
        };
        config.setInputPaths("src/main/java");
        config.setRuleSets("rulesets/java/basic.xml,rulesets/java/codesize.xml,rulesets/java/empty.xml,rulesets/java/design.xml,rulesets/java/coupling.xml,rulesets/java/optimizations.xml");
        config.setReportFormat("csv");
        config.setDebug(true);
        config.setThreads(1);
        PMD.doPMD(config);
    }

    private void setProperty(PMDConfiguration config, String rule, String property, Object value) {
        final Rule r = config.getPmdRuleSets().getRuleByName(rule);
        final PropertyDescriptor<Object> descriptor = (PropertyDescriptor<Object>) r.getPropertyDescriptor(property);
        r.setProperty(descriptor, value);
    }
}
