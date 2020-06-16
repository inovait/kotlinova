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
import androidx.core.app.BundleCompat
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
        is Enum<*> -> putString(key, value.name)
        else -> throw IllegalStateException(
            "Type ${value.javaClass.canonicalName} of property $key is not supported in bundle"
        )
    }
}

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T> SharedPreferences.get(key: String, default: T): T {
    return get(key, default, T::class.java)
}

fun SharedPreferences.getEnum(key: String, default: Any?, klass: Class<Enum<*>>): Any? {
    val enumKey = getString(key, null) ?: return default

    @Suppress("UNNECESSARY_SAFE_CALL")
    return klass.enumConstants?.firstOrNull { it.name == enumKey } ?: default
}

@Suppress("IMPLICIT_CAST_TO_ANY")
fun <T> SharedPreferences.get(key: String, default: T, klass: Class<T>): T {
    if (!contains(key)) {
        return default
    }
    if (Enum::class.java.isAssignableFrom(klass)) {
        @Suppress("UNCHECKED_CAST")
        return getEnum(key, default, klass as Class<Enum<*>>) as T
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
        is Enum<*> -> putString(key, value.name)
        else -> throw IllegalStateException(
            "Type ${value.javaClass.canonicalName} of property $key " +
                "is not supported in SharedPreferences"
        )
    }

    return this
}