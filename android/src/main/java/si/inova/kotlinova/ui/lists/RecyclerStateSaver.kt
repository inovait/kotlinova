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
import androidx.recyclerview.widget.RecyclerView
import si.inova.kotlinova.ui.lists.sections.ListSection
import si.inova.kotlinova.ui.state.StateSaver
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * State saver that saves [RecyclerView]'s state and then loads it at any point in time
 * (whenever data is reloaded).
 *
 * You must manually call [notifyRecyclerViewLoaded]&#40;) or if you are using [ListSection], you can
 * instead call [attach]&#40;) to automatically load data when ListAdapter receives new data.
 *
 * @author Matej Drobnic
 */
class RecyclerStateSaver(
    component: StateSavingComponent,
    private val recycler: RecyclerView,
    key: String
) : StateSaver<Parcelable>() {

    private var alreadyRestored = false

    init {
        register(component.stateSaverManager, key)
    }

    override fun saveState(): Parcelable? {
        return recycler.layoutManager?.onSaveInstanceState()
    }

    fun ignoreNextRestore() {
        alreadyRestored = true
    }

    fun notifyRecyclerViewLoaded() {
        if (alreadyRestored) {
            return
        }

        val lastSavedState = lastSavedState ?: return
        recycler.post {
            recycler.layoutManager?.onRestoreInstanceState(lastSavedState)
        }
        alreadyRestored = true
    }

    fun attach(listSection: ListSection<*, *>) {
        listSection.addUpdateListener(this::notifyRecyclerViewLoaded)
    }
}