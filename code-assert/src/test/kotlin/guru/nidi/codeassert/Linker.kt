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
package guru.nidi.codeassert

import java.util.List
import java.util.*

object Linker {
    private val urlRegex = Regex("(https?://|www\\.)[^\\s\\p{Z}…|”“»<>]+")
    private val hashRegex = Regex("#([^\\s\\p{Z}-:;,+!?()…@#*\"'/|\\[\\]{}`<>\$%^&=”“»~’\u2013\u2014.]+)")
    private val userRegex = Regex("@([^\\s\\p{Z}-:;,+!?()…@#*\"'/|\\[\\]{}`<>\$%^&=”“»~’\u2013\u2014]+)")

    fun twitter(content: String) = user(hash(url(newlines(content)), "https://twitter.com/hashtag/"), "https://twitter.com/")

    fun facebook(content: String) = hash(url(newlines(content)), "https://facebook.com/hashtag/")

    fun instagram(content: String) = user(hash(url(newlines(content)), "https://instagram.com/explore/tags/"), "https://instagram.com/")

    private fun newlines(s: String) = s.replace("\n", "<br>");


    fun url(s: String) =
            urlRegex.replace(s) { res ->
                val value = res.value
                val trim = value.trimEnd('.', ')', ',', '!', '?')
                val rest = value.substring(trim.length)
                """<a href="$trim" target="_blank">$trim</a>$rest"""
            }

    fun hash(s: String, base: String) = try {
        hashRegex.replace(s, """<a href="$base$1" target="_blank" rel="nofollow">#$1</a>""")
    } catch (e: Exception) {
        ""
    }

    fun user(s: String, base: String) =
            userRegex.replace(s) { res ->
                val value = res.groups[1]!!.value
                if (".." in value) res.value
                else {
                    val trim = value.trimEnd('.')
                    val rest = value.substring(trim.length)
                    """<a href="$base$trim" target="_blank" rel="nofollow">@$trim</a>$rest"""
                }
            }
}