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

package si.inova.kotlinova.ui.components

import android.app.Activity
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * Base fragment that provides automatic state saving facilities out of the box
 *
 * @author Matej Drobnic
 */
open class StateSaverFragment : Fragment(), StateSavingComponent {
    override val stateSaverManager = StateSaverManager()

    /**
     * Whether this fragment has just been created for the first time.
     *
     * *true* means fragment has been created for the first time OR when fragment has been
     * recreated after process has been completely destroyed (for example due to low memory).
     *
     * *false* means fragment has been recreated from the previous state
     * (for example when being recreated from configuration change or from backstack)
     */
    var createdForTheFirstTime = true
        private set

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            stateSaverManager.restoreInstance(savedInstanceState)
        }

        super.onCreate(savedInstanceState)

        createdForTheFirstTime = createdForTheFirstTime &&
            (savedInstanceState == null ||
                (context as? Activity)?.lastNonConfigurationInstance == null)
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        stateSaverManager.saveInstance(outState)
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        createdForTheFirstTime = false
    }
}