package si.inova.kotlinova.archcomponents

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import si.inova.kotlinova.coroutines.TestableDispatchers
import java.util.WeakHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val lifecycleJobs = WeakHashMap<Lifecycle, Job>()

private fun Lifecycle.createJob(cancelEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY): Job =
    Job().also { job ->
        addObserver(object : GenericLifecycleObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == cancelEvent) {
                    removeObserver(this)
                    job.cancel()
                }
            }
        })
    }

val Lifecycle.job: Job
    get() = lifecycleJobs[this] ?: createJob().also {
        lifecycleJobs[this] = it
        it.invokeOnCompletion { _ -> lifecycleJobs -= this }
    }

fun LifecycleOwner.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    GlobalScope.launch(TestableDispatchers.Main + lifecycle.job + context, block = block)
}