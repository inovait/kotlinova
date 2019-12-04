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