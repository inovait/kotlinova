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
        if (this.value == value) return
        val arguments = thisRef.arguments ?: Bundle()

        val key = property.name

        arguments[key] = value
        this.value = value
        thisRef.arguments = arguments
    }
}