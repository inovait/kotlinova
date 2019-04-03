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