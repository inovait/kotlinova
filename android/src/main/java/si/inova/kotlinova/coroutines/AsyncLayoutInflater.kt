/**
 * @author Matej Drobnic
 */
@file:JvmName("AsyncLayoutInflaterCoroutines")

package si.inova.kotlinova.coroutines

import android.support.annotation.LayoutRes
import android.support.v4.view.AsyncLayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun AsyncLayoutInflater.inflateAndAwait(@LayoutRes resId: Int, parent: ViewGroup?): View {
    return suspendCancellableCoroutine { continuation ->
        inflate(resId, parent) { view: View, _: Int, _: ViewGroup? ->
            continuation.resume(view)
        }
    }
}