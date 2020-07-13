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

public class ActionResult {
    final boolean accept;
    final Action<?> action;
    final int quality;

    public ActionResult(boolean accept, Action<?> action, int quality) {
        this.accept = accept;
        this.action = action;
        this.quality = quality;
    }

    public static ActionResult accept(Action<?> action, int quality) {
        return new ActionResult(true, action, quality);
    }

    public static ActionResult reject(Action<?> action, int quality) {
        return new ActionResult(false, action, quality);
    }

    public static ActionResult undecided(Action<?> action) {
        return new ActionResult(false, action, 0);
    }

    public static ActionResult acceptIfTrue(boolean accept, Action<?> action, int quality) {
        return accept ? accept(action, quality) : undecided(action);
    }

    public static ActionResult rejectIfFalse(boolean accept, Action<?> action, int quality) {
        return accept ? undecided(action) : reject(action, quality);
    }

    public boolean isUndecided() {
        return quality == 0;
    }

    public ActionResult orMoreQuality(ActionResult result) {
        return quality >= result.quality ? this : result;
    }
}
