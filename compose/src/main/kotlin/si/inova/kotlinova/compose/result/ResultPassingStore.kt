/*
 * Copyright 2023 INOVA IT d.o.o.
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

package si.inova.kotlinova.compose.result

import android.os.Parcelable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.lang.ref.WeakReference

/**
 * A result store that serves as a mediator between result receiver and sender.
 *
 * For result passing to work, you need to remember this store as saveable and provide [LocalResultPassingStore] somewhere near
 * the root of the compose tree (for example in MainActivity). For example:
 *
 * ```kotlin
 * val resultPassingStore = rememberSaveable { ResultPassingStore() }
 *
 * CompositionLocalProvider(LocalResultPassingStore provides resultPassingStore) {
 *     ...
 * }
 * ```
 */
@Parcelize
class ResultPassingStore(private val store: @RawValue HashMap<Int, Any> = HashMap()) : Parcelable {
   @IgnoredOnParcel
   private val callbacks = HashMap<Int, WeakReference<(Any) -> Unit>>()

   @Suppress("UNCHECKED_CAST")
   fun <T> registerCallback(key: Int, callback: (T) -> Unit): ResultKey<T> {
      val existingData = store.remove(key)
      if (existingData != null) {
         callback(existingData as T)
      }

      callbacks += key to WeakReference(callback as (Any) -> Unit)

      return ResultKey(key)
   }

   fun <T> unregisterCallback(callback: (T) -> Unit) {
      callbacks.values.removeIf {
         it.get() == callback
      }
   }

   fun <T : Any> sendResult(key: ResultKey<T>, result: T) {
      val callback = callbacks[key.key]?.get()
      if (callback != null) {
         callback(result)
      } else {
         store[key.key] = result
      }
   }
}

val LocalResultPassingStore = compositionLocalOf<ResultPassingStore> {
   error("Missing LocalResultPassingStore")
}
