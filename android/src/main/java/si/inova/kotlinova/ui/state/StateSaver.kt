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

/**
 * Base class for all classes that automatically save their state through [StateSaverManager].
 *
 * Implementors must call [register] as soon as they have access to their key and to [StateSaverManager].
 *
 * @author Matej Drobnic
 */
abstract class StateSaver<T> {
    private var key: String? = null
    private var stateSaverManager: StateSaverManager? = null

    /**
     *  Register this StateSaver with StateSaverManager. This method executes only once,
     *  but can be called multiple times (all subsequent calls do nothing).
     */
    protected fun register(stateSaverManager: StateSaverManager, key: String) {
        if (this.stateSaverManager != null) {
            return
        }

        stateSaverManager.registerStateSaver(key, this)
        this.stateSaverManager = stateSaverManager
        this.key = key
    }

    protected val lastSavedState: T?
        get() {
            val stateSaver = stateSaverManager
                    ?: throw IllegalStateException("StateSaver not registered")
            return stateSaver.getLastLoadedValue(key!!)
        }

    /**
     * Method that gets automatically called when state gets saved.
     * You must return current state or *null*.
     */
    abstract fun saveState(): T?

    /**
     * Method that gets automatically called when state gets loaded. Override.
     */
    open fun loadState(savedValue: T?) = Unit
}
