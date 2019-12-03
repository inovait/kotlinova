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

import org.junit.rules.ExternalResource
import timber.log.Timber

/**
 * JUnit rule that redirects timber output to System.out
 */
class TimberSystemOutRule : ExternalResource() {
    private val tree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            println(message) // NOTASK
            t?.printStackTrace()
        }
    }

    override fun before() {
        Timber.plant(tree)
    }

    override fun after() {
        Timber.uproot(tree)
    }
}