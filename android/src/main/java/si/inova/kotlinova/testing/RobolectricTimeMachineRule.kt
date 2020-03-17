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

import android.os.SystemClock
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import si.inova.kotlinova.time.AndroidTimeProvider
import si.inova.kotlinova.time.JavaTimeProvider

/**
 * JUnit Rule that controls all values in [JavaTimeProvider] and [AndroidTimeProvider]
 * based on Robolectric's scheduler
 * @author Matej Drobnic
 */
@Deprecated("Use instrumented tests instead of Robolectric")
class RobolectricTimeMachineRule : TestWatcher() {
    override fun finished(description: Description?) {
        JavaTimeProvider.currentTimeMillisProvider = { System.currentTimeMillis() }
        JavaTimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.uptimeMillisProvider = { SystemClock.uptimeMillis() }
        AndroidTimeProvider.clockProvider = { Clock.systemUTC() }
    }

    override fun starting(description: Description?) {
        JavaTimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        JavaTimeProvider.currentTimeMillisProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.uptimeMillisProvider = { SystemClock.elapsedRealtime() }
        AndroidTimeProvider.clockProvider =
            { Clock.fixed(Instant.ofEpochMilli(SystemClock.elapsedRealtime()), ZoneId.of("UTC")) }
    }
}