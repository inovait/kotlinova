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