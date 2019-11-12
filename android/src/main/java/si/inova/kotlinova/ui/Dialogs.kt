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

@file:JvmName("DialogUtils")

package si.inova.kotlinova.ui

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun displayTextDialog(
    context: Context,
    title: String,
    message: String,
    dismissCallback: (() -> Unit)? = null
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, null)
        .apply {
            if (dismissCallback != null) {
                setOnDismissListener { dismissCallback() }
            }
        }
        .show()
}
