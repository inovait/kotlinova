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
import java.util.Collections

/**
 * Test rule that fails any test that have thrown any test exceptions in different threads
 *
 * @author Matej Drobnic
 */
class UncaughtExceptionThrowRule : TestWatcher() {
    var oldExceptionHandler: Thread.UncaughtExceptionHandler? = null

    val errors = Collections.synchronizedList(ArrayList<Throwable>())

    override fun finished(description: Description?) {
        Thread.setDefaultUncaughtExceptionHandler(oldExceptionHandler)
        Thread.currentThread().uncaughtExceptionHandler = oldExceptionHandler
        MultipleFailureException.assertEmpty(errors)
    }

    override fun starting(description: Description?) {
        val newExceptionHandler =
            Thread.UncaughtExceptionHandler { _, e -> errors.add(e) }
        oldExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.currentThread().uncaughtExceptionHandler = newExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(newExceptionHandler)
    }
}