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

import android.Manifest
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import si.inova.kotlinova.testing.espresso.ScreenshotTestRule.Companion.create
import java.io.File

/**
 * Rule that takes a screenshot of the device when test fails.
 *
 * To increase reliability, you should use [create] method to create this rule and pass in rule
 * that performs espresso testing (such as *ActivityRule*). Passed rules must not have its own
 * *@Rule* annotation. This method will also automatically grant itself
 * *WRITE_EXTERNAL_STORAGE* permission which is needed for screenshot writing.
 *
 * To use this rule, you must add *androidx.test.uiautomator:uiautomator* dependency.
 */
class ScreenshotTestRule constructor() : TestWatcher() {
    override fun failed(e: Throwable, description: Description) {
        val path = File(Environment.getExternalStorageDirectory(), "screenshots")

        if (!path.exists()) {
            path.mkdirs()
        }

        // Take advantage of UiAutomator screenshot method
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val filename = "${description.className}-${description.methodName}.png"
        device.takeScreenshot(File(path, filename))
    }

    companion object {
        fun create(wrappedRule: TestRule): TestRule {
            return RuleChain
                .outerRule(
                    GrantPermissionRule.grant(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                .around(wrappedRule)
                .around(ScreenshotTestRule())
        }
    }
}