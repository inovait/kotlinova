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

package si.inova.kotlinova.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.ExternalResource
import si.inova.kotlinova.coroutines.TestableDispatchers

@Suppress("EXPERIMENTAL_API_USAGE")
open class DispatcherReplacementRule(val dispatcher: CoroutineDispatcher) : ExternalResource() {
    override fun before() {
        Dispatchers.setMain(dispatcher)
        TestableDispatchers.dispatcherOverride = { dispatcher }
    }

    override fun after() {
        Dispatchers.resetMain()
        TestableDispatchers.dispatcherOverride = { it() }
    }
}