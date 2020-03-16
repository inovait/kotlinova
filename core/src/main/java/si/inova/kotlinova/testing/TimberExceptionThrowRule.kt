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

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.MultipleFailureException
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 * Test rule that fails any test that have thrown any exceptions through Timber logging
 *
 * @author Matej Drobnic
 */
class TimberExceptionThrowRule : TestWatcher() {
    val errors = Collections.synchronizedList(ArrayList<Throwable>())

    private val tree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, exception: Throwable?) {
            exception?.let { errors.add(it) }
        }
    }

    override fun finished(description: Description?) {
        Timber.uproot(tree)
        MultipleFailureException.assertEmpty(errors)
    }

    override fun starting(description: Description?) {
        Timber.plant(tree)
    }
}