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

package si.inova.kotlinova.compose.result

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
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
class ResultPassingStore(private val store: @RawValue HashMap<ResultKey<*>, Any> = HashMap()) : Parcelable {
   @IgnoredOnParcel
   private val callbacks = HashMap<Long, MutableList<WeakReference<(Any?) -> Unit>?>>()

   @Suppress("UNCHECKED_CAST")
   @VisibleForTesting
   fun <T> registerCallback(compositeKeyHashCode: Long, callback: (T) -> Unit): ResultKey<T> {
      val index = callbacks[compositeKeyHashCode]?.size ?: 0
      val resultKey = ResultKey<T>(compositeKeyHashCode, index)

      val existingData = store.remove(resultKey)
      if (existingData != null) {
         callback(unwrapResult(existingData))
      }

      callbacks.getOrPut(compositeKeyHashCode) { ArrayList() }.add(WeakReference(callback as (Any) -> Unit))

      return resultKey
   }

   @VisibleForTesting
   fun unregisterCallback(key: ResultKey<*>) {
      callbacks[key.compositeKeyHashCode]?.set(key.index, WeakReference(null))
   }

   fun <T : Any> sendResult(key: ResultKey<T>, result: T) {
      val callback = callbacks[key.compositeKeyHashCode]?.elementAtOrNull(key.index)?.get()
      if (callback != null) {
         callback(result)
      } else {
         store[key] = result ?: NullValue
      }

   private fun <T> unwrapResult(result: Any): T {
      @Suppress("UNCHECKED_CAST")
      return if (result === NullValue) {
         null
      } else {
         result
      } as T
   }
}

val LocalResultPassingStore = compositionLocalOf<ResultPassingStore> {
   error("Missing LocalResultPassingStore")
}
