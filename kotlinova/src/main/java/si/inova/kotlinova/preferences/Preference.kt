package si.inova.kotlinova.preferences

import android.content.SharedPreferences
import si.inova.kotlinova.utils.get
import si.inova.kotlinova.utils.put
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
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
        sharedPreferences.edit().put(property.name, value as Any).apply()
    }
}

inline fun <reified T> preference(
    sharedPreferences: SharedPreferences,
    defaultValue: T
): PreferenceProperty<T> {
    return PreferenceProperty(sharedPreferences, defaultValue, T::class.java)
}
