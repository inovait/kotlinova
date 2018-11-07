@file:JvmName("ContextUtils")

package si.inova.kotlinova.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.TypedValue
import androidx.core.content.ContextCompat

/**
 * @author Matej Drobnic
 */

/**
 * Convenience method for easier permission checking
 *
 * @return whether permission is granted or not
 */
fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.fromDpToPixels(value: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
