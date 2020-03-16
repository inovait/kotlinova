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

package si.inova.kotlinova.testing.fakes

import android.content.SharedPreferences

/**
 * Memory based shared preferences without any references to Android SDK
 *
 * Use for unit testing shared preferences
 */
class MemorySharedPreferences : SharedPreferences {
    private val data = hashMapOf<String, Any>()
    private val listeners = ArrayList<SharedPreferences.OnSharedPreferenceChangeListener>()

    private fun mergeFromEdit(editData: MutableMap<String, Any>) {
        for ((key, value) in ArrayList(data.entries)) {
            if (editData[key] != value) {
                data.putOrRemove(key, editData[key])
                notifyChange(key)
            }
        }

        for ((key, value) in ArrayList(editData.entries)) {
            if (data[key] != value) {
                data.putOrRemove(key, value)
                notifyChange(key)
            }
        }
    }

    private fun notifyChange(key: String) {
        for (listener in listeners) {
            listener.onSharedPreferenceChanged(this, key)
        }
    }

    override fun contains(key: String): Boolean {
        return data.contains(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return data[key] as Boolean? ?: defValue
    }

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        listeners.add(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        listeners.remove(listener)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return data[key] as Int? ?: defValue
    }

    override fun getAll(): MutableMap<String, *> {
        return data.toMap() as MutableMap<String, *>
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor(data.toMutableMap())
    }

    override fun getLong(key: String, defValue: Long): Long {
        return data[key] as Long? ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return data[key] as Float? ?: defValue
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        @Suppress("UNCHECKED_CAST")
        return data[key] as Set<String>? ?: defValue
    }

    override fun getString(key: String, defValue: String?): String? {
        return data[key] as String? ?: defValue
    }

    private inner class Editor(
        private val editData: MutableMap<String, Any>
    ) : SharedPreferences.Editor {
        override fun clear(): SharedPreferences.Editor {
            editData.clear()
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            editData[key] = value
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            editData[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            editData.remove(key)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            editData[key] = value
            return this
        }

        override fun putStringSet(
            key: String,
            value: MutableSet<String>?
        ): SharedPreferences.Editor {
            if (value == null) {
                remove(key)
                return this
            }

            editData[key] = value
            return this
        }

        override fun commit(): Boolean {
            mergeFromEdit(editData)
            return true
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            editData[key] = value
            return this
        }

        override fun apply() {
            mergeFromEdit(editData)
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            if (value == null) {
                remove(key)
                return this
            }

            editData[key] = value
            return this
        }
    }

    val hasListeners: Boolean
        get() = listeners.isNotEmpty()
}

/**
 * Put passed entry into map if passed value is non-null, or remove if from the map if null
 */
private fun <K, V> MutableMap<K, in V>.putOrRemove(key: K, value: V?) {
    if (value == null) {
        remove(key)
    } else {
        put(key, value)
    }
}