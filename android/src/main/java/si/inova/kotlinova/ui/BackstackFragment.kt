package si.inova.kotlinova.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import si.inova.kotlinova.android.R
import si.inova.kotlinova.ui.components.NestedAnimatedFragment

/**
 * Fragment that has its own backstack
 *
 * @author Matej Drobnic
 */
class BackstackFragment : NestedAnimatedFragment(), ResettableFragment {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.single_container, container, false)
    }

    override fun resetFragment() {
        while (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStackImmediate()
        }
        val currentFragment = childFragmentManager.findFragmentById(R.id.container)
        if (currentFragment is ResettableFragment) {
            currentFragment.resetFragment()
        }
    }

    fun addFragment(fragment: Fragment) {
        val useBackStack = childFragmentManager.findFragmentById(R.id.container) != null

        childFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.container, fragment)
            .apply {
                if (useBackStack) {
                    addToBackStack(null)
                    commit()
                } else {
                    commitNow()
                }
            }
    }

    companion object {
        fun create(): Fragment {
            return BackstackFragment()
        }
    }
}