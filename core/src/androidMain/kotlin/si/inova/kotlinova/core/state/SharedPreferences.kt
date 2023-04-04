package si.inova.kotlinova.core.state

import android.content.SharedPreferences

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T> SharedPreferences.get(key: String, default: T): T {
   return get(key, default, T::class.java)
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun <T> SharedPreferences.get(key: String, default: T, klass: Class<T>): T {
   if (!contains(key)) {
      return default
   }

   @Suppress("UNCHECKED_CAST")
   return when (klass) {
      String::class.java -> getString(key, default as String?)
      Int::class.java -> getInt(key, default as Int)
      java.lang.Integer::class.java -> getInt(key, (default as Int?) ?: 0)
      Long::class.java -> getLong(key, default as Long)
      java.lang.Long::class.java -> getLong(key, (default as Long?) ?: 0)
      Float::class.java -> getFloat(key, default as Float)
      java.lang.Float::class.java -> getFloat(key, (default as Float?) ?: 0f)
      Boolean::class.java -> getBoolean(key, default as Boolean)
      java.lang.Boolean::class.java -> getBoolean(key, (default as Boolean?) ?: false)
      Set::class.java -> getStringSet(key, default as Set<String>?)
      else -> error(
         "Type ${klass.canonicalName} of property $key is not supported in SharedPreferences"
      )
   } as T
}

fun SharedPreferences.Editor.put(key: String, value: Any): SharedPreferences.Editor {
   @Suppress("UNCHECKED_CAST")
   when (value) {
      is String -> putString(key, value)
      is Int -> putInt(key, value)
      is Long -> putLong(key, value)
      is Float -> putFloat(key, value)
      is Boolean -> putBoolean(key, value)
      is Set<*> -> putStringSet(key, value as MutableSet<String>)
      else -> error(
         "Type ${value.javaClass.canonicalName} of property $key " +
            "is not supported in SharedPreferences"
      )
   }

   return this
}
