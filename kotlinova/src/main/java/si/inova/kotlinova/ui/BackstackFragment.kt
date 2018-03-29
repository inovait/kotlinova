package si.inova.kotlinova.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.inova.kotlinova.R
import si.inova.kotlinova.ui.components.NestedAnimatedFragment

/**
 * Fragment that has its own backstack
 *
 * @author Matej Drobnic
 */
class BackstackFragment : NestedAnimatedFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.single_container, container, false)
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
                    }
                }
                .commit()
    }

    companion object {
        fun create(): Fragment {
            return BackstackFragment()
        }
    }
}