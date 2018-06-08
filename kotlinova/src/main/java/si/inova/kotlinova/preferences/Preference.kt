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
