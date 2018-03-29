/**
 * Methods for helping universal saving into various android storage classes
 *
 * @author Matej Drobnic
 */
@file:JvmName("StorageUtils")

package si.inova.kotlinova.utils

import android.content.SharedPreferences
import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.BundleCompat
import java.io.Serializable

/**
 * Helper single method to set any object into bundle without using different set methods for different types
 */
operator fun Bundle.set(key: String, value: Any) {
    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Short -> putShort(key, value)
        is Long -> putLong(key, value)
        is Byte -> putByte(key, value)
        is ByteArray -> putByteArray(key, value)
        is Char -> putChar(key, value)
        is CharArray -> putCharArray(key, value)
        is CharSequence -> putCharSequence(key, value)
        is Float -> putFloat(key, value)
        is Bundle -> putBundle(key, value)
        is Binder -> BundleCompat.putBinder(this, key, value)
        is Parcelable -> putParcelable(key, value)
        is Serializable -> putSerializable(key, value)
        else -> throw IllegalStateException(
                "Type ${value.javaClass.canonicalName} of property $key is not supported in bundle"
        )
    }
}

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T> SharedPreferences.get(key: String, default: T): T {
    return get(key, default, T::class.java)
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun <T> SharedPreferences.get(key: String, default: T, klass: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    return when (klass) {
        String::class.java -> getString(key, default as String?)
        Int::class.java -> getInt(key, default as Int)
        java.lang.Integer::class.java -> getInt(key, default as Int)
        Long::class.java -> getLong(key, default as Long)
        java.lang.Long::class.java -> getLong(key, default as Long)
        Float::class.java -> getFloat(key, default as Float)
        java.lang.Float::class.java -> getFloat(key, default as Float)
        Boolean::class.java -> getBoolean(key, default as Boolean)
        java.lang.Boolean::class.java -> getBoolean(key, default as Boolean)
        Set::class.java -> getStringSet(key, default as Set<String>?)
        else -> throw IllegalStateException(
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
        else -> throw IllegalStateException(
                "Type ${value.javaClass.canonicalName} of property $key " +
                        "is not supported in SharedPreferences"
        )
    }

    return this
}