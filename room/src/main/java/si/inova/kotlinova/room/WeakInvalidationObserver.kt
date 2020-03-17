/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

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