/**
 * @author Matej Drobnic
 */
@file:JvmName("ThreadUtils")

package si.inova.kotlinova.utils

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Run specified block on the UI thread.
 *
 * If caller is already on the UI thread, block is ran immediately synchronously.
 * Otherwise block is scheduled to run on UI thread scheduler.
 *
 * @param parentJob If task gets launched on UI thread,
 * it will be started as a child of this job. Can be *null*.
 */
fun runOnUiThread(
    parentJob: Job? = null,
    block: () -> Unit
) {
    @SuppressLint("RestrictedApi")
    if (ArchTaskExecutor.getInstance().isMainThread) {
        block()
    } else {
        val context = if (parentJob == null) {
            Dispatchers.Main
        } else {
            Dispatchers.Main + parentJob
        }

        GlobalScope.launch(context, CoroutineStart.DEFAULT, {
            block()
        })
    }
}
