/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
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