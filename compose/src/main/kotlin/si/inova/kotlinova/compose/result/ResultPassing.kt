package si.inova.kotlinova.compose.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositeKeyHash

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
fun <T : Any> registerResultReceiver(callback: (T) -> Unit): ResultKey<T> {
   val store = LocalResultPassingStore.current
   val key = currentCompositeKeyHash

   val resultKey = store.registerCallback(key, callback)

   DisposableEffect(callback) {
      onDispose {
         store.unregisterCallback(callback)
      }
   }

   return resultKey
}

@Composable
fun <T : Any> ResultKey<T>.SendResult(value: T) {
   val store = LocalResultPassingStore.current

   store.sendResult(this, value)
}
