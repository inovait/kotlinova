package si.inova.kotlinova.room

import androidx.room.InvalidationTracker
import java.lang.ref.WeakReference

/**
 * Reimplementation of Google's WeakObserver
 *
 * original cannot be used beacuse it is packate-protected inside Room library
 */
class WeakInvalidationObserver(
    tables: Array<out String>,
    private val invalidationTracker: InvalidationTracker,
    trigger: () -> Unit
) : InvalidationTracker.Observer(tables) {
    private val triggerRef: WeakReference<() -> Unit> = WeakReference(trigger)

    override fun onInvalidated(tables: MutableSet<String>) {
        val trigger = triggerRef.get()
        if (trigger == null) {
            invalidationTracker.removeObserver(this)
        } else {
            trigger()
        }
    }
}