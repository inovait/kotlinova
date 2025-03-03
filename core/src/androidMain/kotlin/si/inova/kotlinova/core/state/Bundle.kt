/*
 * Copyright 2024 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.core.state

import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
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
      is Binder -> putBinder(key, value)
      is Parcelable -> putParcelable(key, value)
      is Serializable -> putSerializable(key, value)
      else -> error(
         "Type ${value.javaClass.canonicalName} of property $key is not supported in bundle"
      )
   }
}

/**
 * Convert bundle to regular map. Mostly useful for logging.
 */
@Deprecated("Naming was not indicative of what that function does. Use toMap() instead.", ReplaceWith("toMap()"))
fun Bundle.asMap(): Map<String, Any?> {
   return toMap()
}

/**
 * Convert bundle to regular map. Mostly useful for logging.
 */
fun Bundle.toMap(): Map<String, Any?> {
   return keySet().associateWith {
      @Suppress("DEPRECATION") // We cannot use type safe APIs in this case
      get(it)
   }
}
