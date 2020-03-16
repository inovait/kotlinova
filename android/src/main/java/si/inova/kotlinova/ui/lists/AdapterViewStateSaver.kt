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

package si.inova.kotlinova.ui.lists

import android.os.Parcelable
import android.util.SparseArray
import android.view.View
import android.widget.AdapterView
import si.inova.kotlinova.ui.state.StateSaver
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * State saver that saves any [AdapterView]'s state and then loads it at any point in time
 * (whenever data is reloaded).
 *
 * You must manually call [notifyDataLoaded]&#40;) whenever data gets loaded into linked AdapterView.
 *
 * @author Matej Drobnic
 */
class AdapterViewStateSaver(
        component: StateSavingComponent,
        private val adapterView: AdapterView<*>,
        key: String
) : StateSaver<Parcelable>() {

    private var alreadyRestored = false

    init {
        register(component.stateSaverManager, key)
    }

    override fun saveState(): Parcelable? {
        val viewId = adapterView.id
        if (viewId == View.NO_ID) {
            throw IllegalStateException("To save state, AdapterView must have an ID!")
        }

        val container = SparseArray<Parcelable>()
        adapterView.saveHierarchyState(container)
        return container.get(viewId)
    }

    fun notifyDataLoaded() {
        val lastSavedState = lastSavedState ?: return
        if (alreadyRestored) {
            return
        }

        val viewId = adapterView.id
        if (viewId == View.NO_ID) {
            throw IllegalStateException("To save state, AdapterView must have an ID!")
        }

        val container = SparseArray<Parcelable>()
        container.put(viewId, lastSavedState)
        adapterView.restoreHierarchyState(container)
        alreadyRestored = true
    }
}