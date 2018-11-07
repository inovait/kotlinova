package si.inova.kotlinova.ui.arguments

import android.os.Bundle
import androidx.fragment.app.Fragment
import si.inova.kotlinova.utils.set
import kotlin.reflect.KProperty

/**
 * Property delegate for persistable fragment arguments that can be null (check are skipepd).
 *
 * Allows for very easy argument declaration
 * without manually messing around with fragment argument bundles.
 */
class NullableFragmentArgument<T : Any> : kotlin.properties.ReadWriteProperty<Fragment, T?> {

    var value: T? = null

    override operator fun getValue(thisRef: Fragment, property: KProperty<*>): T? {
        if (value == null) {
            val args = thisRef.arguments
                ?: throw IllegalStateException(
                    "Cannot read property ${property.name} if no arguments have been set"
                )
            @Suppress("UNCHECKED_CAST")
            value = args.get(property.name) as T?
        }
        return value
    }

    override operator fun setValue(thisRef: Fragment, property: KProperty<*>, value: T?) {
        val arguments = thisRef.arguments ?: Bundle()

        val key = property.name

        if (value == null) {
            arguments.remove(key)
        } else {
            arguments[key] = value
        }

        thisRef.arguments = arguments
    }
}