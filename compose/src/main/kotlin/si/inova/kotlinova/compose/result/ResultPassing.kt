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

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHashCode

/**
 * Register a receiver that can receive result data from another screen. This method will return a [ResultKey]
 * that you have to pass to another screen.
 *
 * That screen can then call [ResultKey.SendResult] or manually call [ResultPassingStore.sendResult] to send a result back to this
 * receiver.
 *
 * If both screens are active and displayed, [callback] will be triggered immediately upon result being sent. Otherwise,
 * callback will get triggered whenever this screen comes back on screen. Passed result will survive configuration changes and
 * process kills.
 *
 * For this system to work, compose tree needs to have a [LocalResultPassingStore] (it should be initialized somewhere near the
 * root of the compose tree, for example in the Main Activity).
 *
 * Only parcelizable types (https://developer.android.com/kotlin/parcelize#types) are supported as a result types.
 */
@Composable
@SuppressLint("VisibleForTests") // This is the primary use :)
fun <T : Any> registerResultReceiver(callback: (T) -> Unit): ResultKey<T> {
   val store = LocalResultPassingStore.current
   val key = currentCompositeKeyHashCode

   val resultKey = store.registerCallback(key, callback)

   DisposableEffect(callback) {
      onDispose {
         store.unregisterCallback(resultKey)
      }
   }

   return resultKey
}

@Composable
fun <T : Any> ResultKey<T>.SendResult(value: T) {
   val store = LocalResultPassingStore.current

   store.sendResult(this, value)
}
