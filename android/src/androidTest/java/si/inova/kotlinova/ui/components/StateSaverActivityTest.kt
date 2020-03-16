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

package si.inova.kotlinova.ui.components

import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.StateSavingTestActivity

/**
 * @author Matej Drobnic
 */
class StateSaverActivityTest {
    @get:Rule
    val rule = ActivityTestRule(StateSavingTestActivity::class.java, true, true)

    @Test
    fun restoreSavedDataAfterConfigurationChange() {
        val originalActivity = rule.activity

        originalActivity.testInt = 20
        assertFalse(originalActivity.set)
        originalActivity.testString = "TeST"
        assertTrue(originalActivity.set)

        assertEquals("NONDEFINED", originalActivity.customStateSaver.value)
        originalActivity.customStateSaver.value = "TestValue"

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            originalActivity.recreate()
        }
        Espresso.onIdle()

        val recreatedActivity = rule.activity

        assertEquals(20, recreatedActivity.testInt)
        assertEquals("TeST", recreatedActivity.testString)
        assertEquals("TestValue", originalActivity.customStateSaver.value)
        assertFalse(recreatedActivity.set)
    }
}