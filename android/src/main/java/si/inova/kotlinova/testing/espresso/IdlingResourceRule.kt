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

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.reflect.KClass

class IdlingResourceRule(private val resource: IdlingResource) : TestWatcher() {
    override fun starting(description: Description) {
        if (hasIgnoringAnnotation(description)) {
            return
        }

        IdlingRegistry.getInstance().register(resource)

        if (resource is ActivityLifecycleCallback) {
            ActivityLifecycleMonitorRegistry
                    .getInstance()
                    .addLifecycleCallback(resource)
        }

        super.starting(description)
    }

    override fun finished(description: Description) {
        super.finished(description)

        IdlingRegistry.getInstance().unregister(resource)

        if (resource is ActivityLifecycleCallback) {
            ActivityLifecycleMonitorRegistry
                    .getInstance()
                    .removeLifecycleCallback(resource)
        }
    }

    private fun hasIgnoringAnnotation(description: Description): Boolean {
        val annotation = description.getAnnotation(IgnoreIdlingResource::class.java) ?: return false

        return annotation.types.contains(resource::class)
    }
}

/**
 * Ignore specific idling resource on this test.
 *
 * This is useful when test methods tests something that would espresso otherwise wait to be over,
 * for example checking for progress bars.
 *
 * This annotation only has effect when idling resources are created with [IdlingResourceRuleEx]
 * rule.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class IgnoreIdlingResource(vararg val types: KClass<out IdlingResource>)