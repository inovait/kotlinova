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