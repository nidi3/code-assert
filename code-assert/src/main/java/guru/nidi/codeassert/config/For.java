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
package guru.nidi.codeassert.config;

/**
 *
 */
public final class For {
    private final String pack;
    private final String clazz;

    private For(String pack, String clazz) {
        this.pack = pack;
        this.clazz = clazz;
    }

    public static For global() {
        return new For("", "");
    }

    public static For allPackages() {
        return new For("*", "");
    }

    public static For allClasses() {
        return new For("*", "*");
    }

    public static For packge(String pack) {
        return new For(pack, "");
    }

    public static For allInPackage(String pack) {
        return new For(pack, "*");
    }

    public static For clazz(String clazz) {
        final int pos = clazz.lastIndexOf('.');
        return pos < 0 ? new For("*", clazz) : new For(clazz.substring(0, pos), clazz.substring(pos + 1));
    }

    public static For clazz(Class<?> clazz) {
        return clazz(clazz.getName());
    }

    public Minima setMinima(int... values) {
        return new Minima(pack, clazz, values);
    }

    public Minima setNoMinima() {
        return new Minima(pack, clazz);
    }
}
