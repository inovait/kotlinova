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