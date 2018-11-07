@file:JvmName("LogUtils")

package si.inova.kotlinova.utils

import android.os.Bundle
import timber.log.Timber

/**
 * @author Matej Drobnic
 */

/**
 * Method that dumps bundle contents into [Timber].
 */
fun Bundle.dump() {
    Timber.i("BundleDump: (%d entries)", size())
    for (key in keySet()) {
        val value = get(key)
        if (value is Bundle) {
            Timber.i("%s :", key)
            value.dump()
        } else {
            Timber.i("%s: %s", key, value?.toString())
        }
    }
}
