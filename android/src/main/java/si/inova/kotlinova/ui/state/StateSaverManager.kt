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

package si.inova.kotlinova.ui.state

import android.os.Bundle
import androidx.collection.ArrayMap
import si.inova.kotlinova.testing.OpenForTesting
import si.inova.kotlinova.utils.set

/**
 * @author Matej Drobnic
 *
 * Helper class that handles state savings of [StateSaver].
 */

@OpenForTesting
class StateSaverManager {
    private var lastLoadedBundle: Bundle? = null
    private val variablesMap = ArrayMap<String, StateSaver<*>>()

    fun registerStateSaver(key: String, stateSaver: StateSaver<*>) {
        variablesMap[key] = stateSaver

        if (lastLoadedBundle != null) {
            stateSaver.loadState(getLastLoadedValue(key))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getLastLoadedValue(key: String): T? {
        return lastLoadedBundle?.get(key) as T?
    }

    fun restoreInstance(state: Bundle) {
        lastLoadedBundle = Bundle(state)

        for ((key, stateSaver) in variablesMap) {
            stateSaver.loadState(getLastLoadedValue(key))
        }
    }

    fun saveInstance(state: Bundle) {
        for ((key, stateSaver) in variablesMap) {
            val targetValue = stateSaver.saveState()

            if (targetValue == null) {
                state.remove(key)
            } else {
                state[key] = targetValue
            }
        }
    }
}