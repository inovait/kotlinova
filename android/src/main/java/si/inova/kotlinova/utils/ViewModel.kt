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

@file:JvmName("ViewModelUtils")

package si.inova.kotlinova.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

/**
 * @author Matej Drobnic
 */

/**
 * Convenience method to quickly load [ViewModel] without manually specifying its class.
 */
inline fun <reified T : ViewModel> ViewModelProvider.load(): T {
    return get(T::class.java)
}

/**
 * Create ViewModel for specified activity
 */
inline fun <reified T : ViewModel> ViewModelProvider.Factory.create(activity: FragmentActivity): T {
    return ViewModelProviders.of(activity, this).load()
}

/**
 * Create ViewModel for specified fragment
 */
inline fun <reified T : ViewModel> ViewModelProvider.Factory.create(fragment: Fragment): T {
    return ViewModelProviders.of(fragment, this).load()
}

/**
 * Create ViewModel for activity that hosts specified fragment.
 * Used to share same ViewModel instances across different fragments inside activity.
 */
inline fun <reified T : ViewModel> ViewModelProvider.Factory.createFromActivity(
    fragment: Fragment
): T {
    return ViewModelProviders.of(fragment.requireActivity(), this).load()
}