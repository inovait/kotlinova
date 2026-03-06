/*
 * Copyright 2026 INOVA IT d.o.o.
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

package si.inova.kotlinova.navigation.util

import android.os.Parcelable
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateWriter
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import java.io.Serializable

/**
 * Helper single method to set any object into SavedState without using different set methods for different types
 */
actual fun SavedStateWriter.put(
   serializer: KSerializer<*>?,
   serializersModule: SerializersModule,
   key: String,
   value: Any?,
) {
   when {
      value == null -> putNull(key)
      serializer != null -> {
         @Suppress("UNCHECKED_CAST")
         putSavedState(
            key,
            encodeToSavedState(
               serializer as KSerializer<Any>,
               value,
               SavedStateConfiguration {
                  this.serializersModule = serializersModule
               }
            )
         )
      }

      value is String -> putString(key, value)
      value is Boolean -> putBoolean(key, value)
      value is BooleanArray -> putBooleanArray(key, value)
      value is Int -> putInt(key, value)
      value is IntArray -> putIntArray(key, value)
      value is Long -> putLong(key, value)
      value is LongArray -> putLongArray(key, value)
      value is Char -> putChar(key, value)
      value is CharArray -> putCharArray(key, value)
      value is CharSequence -> putCharSequence(key, value)
      value is Float -> putFloat(key, value)
      value is SavedState -> putSavedState(key, value)
      value is Parcelable -> putParcelable(key, value)
      value is Serializable -> putJavaSerializable(key, value)
      else -> error(
         "Type ${value.javaClass.canonicalName} of property $key is not supported in saved state"
      )
   }
}

actual fun SavedState.get(
   serializer: KSerializer<*>?,
   serializersModule: SerializersModule,
   key: String,
): Any? {
   @Suppress("DEPRECATION") // Much better option than
   val value = get(key)
   return if (value is SavedState) {
      if (serializer != null) {
         decodeFromSavedState(
            serializer,
            value,
            SavedStateConfiguration {
               this.serializersModule = serializersModule
            }
         )
      } else {
         value
      }
   } else {
      value
   }
}
