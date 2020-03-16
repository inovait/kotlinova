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

package si.inova.kotlinova.testing.espresso

import android.app.PendingIntent
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/**
 * Utility object that roates testing device into portrait/landscape
 */
object DeviceRotator {
    fun forcePortrait() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .setOrientationNatural()
    }

    fun forceLandscape() {
        // HACK: Open default dialer application since stock home screen cannot go into
        // landscape

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext

        val intent = context.packageManager
                .getLaunchIntentForPackage("com.google.android.dialer")
                ?: error("Dialer app is not installed")

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // HACK: App cannot open itself after user pressed home button
        // Use pending intent as a workaround
        val pendingIntent = PendingIntent.getActivity(context, 999, intent, 0)
        pendingIntent.send()

        UiDevice.getInstance(instrumentation).apply {
            waitForIdle()
            setOrientationLeft()
            waitForIdle()
        }
    }

    fun resetRotation() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).apply {
            unfreezeRotation()

            // Go back home from dialer
            pressHome()
            waitForIdle()
        }
    }
}