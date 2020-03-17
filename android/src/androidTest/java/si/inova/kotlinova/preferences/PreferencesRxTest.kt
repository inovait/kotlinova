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

package si.inova.kotlinova.preferences

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.reactivex.observers.BaseTestConsumer
import junit.framework.Assert.assertFalse
import org.junit.After
import org.junit.Test
import si.inova.kotlinova.testing.fakes.MemorySharedPreferences

class PreferencesRxTest {
    private val preferences = InstrumentationRegistry.getInstrumentation()
        .targetContext.getSharedPreferences("test", Context.MODE_PRIVATE)

    @After
    fun tearDown() {
        preferences.edit().clear().apply()
    }

    @Test
    fun receiveUpdates() {
        val observable = preferences.updatesObservable()

        val subscriber = observable.test()

        preferences.edit()
            .putInt("A", 10)
            .apply()

        preferences.edit()
            .putInt("B", 20)
            .apply()

        preferences.edit()
            .putInt("C", 30)
            .apply()

        subscriber.awaitCount(
            3,
            BaseTestConsumer.TestWaitStrategy.SLEEP_10MS,
            5_000
        )
        subscriber.assertValues("A", "B", "C")
    }

    @Test
    fun unregisterOnDispose() {
        val memoryPreferences = MemorySharedPreferences()
        val observable = memoryPreferences.updatesObservable()

        val subscriber = observable.test()

        subscriber.dispose()

        assertFalse(memoryPreferences.hasListeners)
    }
}