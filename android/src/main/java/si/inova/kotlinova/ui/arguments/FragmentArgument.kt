package si.inova.kotlinova.ui.arguments

import android.os.Bundle
import androidx.fragment.app.Fragment
import si.inova.kotlinova.utils.set
import kotlin.reflect.KProperty

/**
 * Property delegate for persistable fragment arguments.
 *
 * Allows for very easy argument declaration
 * without manually messing around with fragment argument bundles.
 */
class FragmentArgument<T : Any> : kotlin.properties.ReadWriteProperty<Fragment, T> {

    var value: T? = null

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (value == null) {
            val args = thisRef.arguments
                ?: throw IllegalStateException(
                    "Cannot read property ${property.name} if no arguments have been set"
                )
            @Suppress("UNCHECKED_CAST")
            value = args.get(property.name) as T
        }

        return value ?: throw IllegalStateException("Property ${property.name} could not be read")
    }

    override operator fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        val arguments = thisRef.arguments ?: Bundle()

        val key = property.name

        arguments[key] = value
        thisRef.arguments = arguments
    }
}