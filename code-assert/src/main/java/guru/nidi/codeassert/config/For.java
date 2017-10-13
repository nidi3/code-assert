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
package guru.nidi.codeassert.config;

public final class For {
    private final Location loc;
    private final String pack;
    private final String clazz;

    private For(Location loc, String pack, String clazz) {
        this.loc = loc;
        this.pack = pack;
        this.clazz = clazz;
    }

    public static For global() {
        return new For(null, "", "");
    }

    public static For allPackages() {
        return new For(null, "*", "");
    }

    public static For allClasses() {
        return new For(null, "*", "*");
    }

    public static For thePackage(String pack) {
        return new For(null, pack, "");
    }

    public static For allInPackage(String pack) {
        return new For(null, pack, "*");
    }

    public static For loc(String loc) {
        return new For(Location.of(loc), null, null);
    }

    public static For clazz(Class<?> clazz) {
        return loc(clazz.getName());
    }

    public Minima setMinima(int... values) {
        return new Minima(loc, pack, clazz, values);
    }

    public Minima setNoMinima() {
        return new Minima(loc, pack, clazz);
    }
}
