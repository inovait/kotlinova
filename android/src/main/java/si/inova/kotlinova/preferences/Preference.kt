/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.preferences

import android.content.SharedPreferences
import si.inova.kotlinova.utils.get
import si.inova.kotlinova.utils.put
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate that wraps [SharedPreferences] and allows easy preference reading and writing
 * with minimal boilerplate.
 *
 * Note that *null* values are not persisted. if [T] is nullable and you try to set *null* value,
 * preference will be removed and [defaultValue] will be returned on the next get.
 *
 * @author Matej Drobnic
 */
class PreferenceProperty<T>(
    private val sharedPreferences: SharedPreferences,
    private val defaultValue: T,
    private val klass: Class<T>
) : ReadWriteProperty<Any, T> {
    @Volatile
    private var cache: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (cache == null) {
            cache = sharedPreferences.get(property.name, defaultValue, klass)
        }

        @Suppress("UNCHECKED_CAST")
        return cache as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        cache = value
        sharedPreferences.edit().apply {
            if (value == null) {
                remove(property.name)
            } else {
                put(property.name, value as Any)
            }
        }.apply()
    }
}

/**
 * Convenience function for easy [PreferenceProperty] creation.
 */
inline fun <reified T> preference(
    sharedPreferences: SharedPreferences,
    defaultValue: T
): PreferenceProperty<T> {
    return PreferenceProperty(sharedPreferences, defaultValue, T::class.java)
}
