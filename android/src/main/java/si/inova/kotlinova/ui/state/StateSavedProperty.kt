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

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property that gets automatically saved and restored when Activity/Fragment gets recreated.
 *
 * Your Activity/Fragment must implement [StateSavingComponent] for this to work.
 *
 * @author Matej Drobnic
 *
 * @param defaultValue Default value that property has
 * @param setNotification Lambda that gets triggered whenever this property is manually set.
 * This will NOT get triggered when value gets restored when Activity/Fragment gets recreated
 */
class StateSavedProperty<T>(
        defaultValue: T,
        private val setNotification: ((T) -> Unit)? = null
) : StateSaver<T>(), ReadWriteProperty<StateSavingComponent, T> {
    var value: T = defaultValue
    var initialized = false

    override fun getValue(thisRef: StateSavingComponent, property: KProperty<*>): T {
        if (!initialized) {
            register(thisRef.stateSaverManager, property.name)

            val savedValue = lastSavedState
            if (savedValue != null) {
                value = savedValue
            }

            initialized = true
        }

        return value
    }

    override fun setValue(thisRef: StateSavingComponent, property: KProperty<*>, value: T) {
        initialized = true
        this.value = value

        register(thisRef.stateSaverManager, property.name)
        setNotification?.invoke(value)
    }

    override fun saveState(): T? {

        return if (initialized) {
            value
        } else {
            null
        }
    }
}