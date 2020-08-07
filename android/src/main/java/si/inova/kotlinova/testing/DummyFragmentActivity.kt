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

package si.inova.kotlinova.testing

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import si.inova.kotlinova.android.R

/**
 * Dummy activity class for testing fragments
 */
open class DummyFragmentActivity : FragmentActivity() {
    private var targetFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.single_container)
        attemptSetup()
    }

    protected open fun attemptSetup() {
        val targetFragment = targetFragment ?: return

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, targetFragment)
            .addToBackStack(targetFragment::class.java.name)
            .commitAllowingStateLoss()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        attemptSetup()

        super.onConfigurationChanged(newConfig)
    }

    open fun swapFragment(fragment: Fragment) {
        targetFragment = fragment
        attemptSetup()
    }
}