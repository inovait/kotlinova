@file:JvmName("Intents")

package si.inova.kotlinova.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * @author Matej Drobnic
 */

/**
 * Method that opens system's web browser (or app if it supports deep linking) on specific URL
 */
fun openWebBrowser(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

/**
 * Method that opens system's dialer with specific number ready to call
 */
fun openDialer(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$number")

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

/**
 * Method that opens Google Maps (either in app or in browser if not installed) and places marker
 * on specific location.
 */
fun openMapWithMarker(context: Context, lat: Double, lon: Double) {
    val link = "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    openWebBrowser(context, link)
}

/**
 * Open user's email client to begin composing email to specified recipient.
 *
 * @return *true* if action succeeded or *false* if user does not have e-mail client installed.
 */
fun composeEmail(context: Context, recipient: String): Boolean {
    val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", recipient, null)
    )

    return try {
        context.startActivity(emailIntent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}