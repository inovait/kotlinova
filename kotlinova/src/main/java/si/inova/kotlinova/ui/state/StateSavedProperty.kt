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