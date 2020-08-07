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

package si.inova.kotlinova.ui.arguments

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A lazy property that gets cleaned up when the fragment's view is destroyed.
 *
 * Accessing this variable while the fragment's view is destroyed will throw [IllegalStateException].
 *
 * This avoids a memory leak and prevent crashing while stacking fragments and going back.
 *
 * Adapted from https://halcyonmobile.com/blog/mobile-app-development/android-app-development/patching-fragment-memory-leaks-2019/
 */
class FragmentAutoClearedValue<T : Any> : ReadWriteProperty<Fragment, T>, DefaultLifecycleObserver {
    private var _value: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
        _value ?: throw IllegalStateException(
            "Trying to call an auto-cleared value outside " +
                "of the view lifecycle."
        )

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        lifecycle.removeObserver(this)
        _value = value
        lifecycle.addObserver(this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        _value = null
    }
}