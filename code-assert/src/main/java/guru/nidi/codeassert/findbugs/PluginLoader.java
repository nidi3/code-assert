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
package guru.nidi.codeassert.findbugs;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.plugins.DuplicatePluginIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

final class PluginLoader {
    private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);
    private static final List<Plugin> PLUGINS = loadPlugins();

    private PluginLoader() {
    }

    static void addPluginsTo(Project project) {
        for (final Plugin plugin : PLUGINS) {
            project.setPluginStatusTrinary(plugin.getPluginId(), true);
        }
    }

    private static List<Plugin> loadPlugins() {
        final List<Plugin> res = new ArrayList<>();
        final Enumeration<URL> resources = findPluginUrls();
        while (resources.hasMoreElements()) {
            try {
                final Plugin plugin = Plugin.addCustomPlugin(jarUrl(resources.nextElement()));
                if (plugin != null) {
                    res.add(plugin);
                    LOG.info("Loaded plugin {}", plugin.getPluginId());
                }
            } catch (DuplicatePluginIdException e) {
                //ignore
            } catch (MalformedURLException | PluginException e) {
                LOG.warn("Problem loading plugin", e);
            }
        }
        return res;
    }

    private static Enumeration<URL> findPluginUrls() {
        try {
            return Thread.currentThread().getContextClassLoader().getResources("findbugs.xml");
        } catch (IOException e) {
            return new Vector<URL>().elements();
        }
    }

    private static URL jarUrl(URL url) throws MalformedURLException {
        final String s = url.toString();
        if (!s.startsWith("jar:file:")) {
            throw new MalformedURLException("Cannot handle plugin URL " + url);
        }
        final int pos = s.indexOf(".jar!/");
        return new URL(s.substring(4, pos) + ".jar");
    }

}
