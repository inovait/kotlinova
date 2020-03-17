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