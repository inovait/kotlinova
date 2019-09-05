package si.inova.kotlinova.archcomponents

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * [LifecycleOwner] that is always resumed
 */
object AlwaysActiveLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    init {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }
}
