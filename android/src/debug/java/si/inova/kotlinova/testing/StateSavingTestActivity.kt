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

package si.inova.kotlinova.testing

import si.inova.kotlinova.ui.components.StateSaverActivity
import si.inova.kotlinova.ui.state.StateSavedProperty
import si.inova.kotlinova.ui.state.StateSaver
import si.inova.kotlinova.ui.state.StateSavingComponent

class StateSavingTestActivity : StateSaverActivity() {
    var testInt by StateSavedProperty<Int>(0)
    var testString by StateSavedProperty<String>("") {
        set = true
    }

    var set = false

    val customStateSaver = CustomStateSaver(this)
}

class CustomStateSaver(stateSavingComponent: StateSavingComponent) : StateSaver<String>() {
    var value = "NONDEFINED"

    init {
        register(stateSavingComponent.stateSaverManager, "CustomStateSaver")
    }

    override fun saveState(): String? {
        return value
    }

    override fun loadState(savedValue: String?) {
        if (savedValue != null) {
            value = savedValue
        }
    }
}