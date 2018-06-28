package si.inova.kotlinova.logging

import timber.log.Timber

/**
 * A [timber.log.Timber.Tree] that sets tag to app name and includes actual tag in the message.
 * This is useful for viewing logs on the phone as most logcat apps do not support per-app filtering.
 *
 *
 * It also makes sure debug and verbose logs are only spewed out when debugging.
 */
class AppTaggedDebugTree(private val appTag: String) : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, appTag, "[$tag] $message", t)
    }
}