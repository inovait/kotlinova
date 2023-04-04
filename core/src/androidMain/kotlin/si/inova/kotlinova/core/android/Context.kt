package si.inova.kotlinova.core.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Convenience method for easier permission checking
 *
 * @return whether permission is granted or not
 */
fun Context.isPermissionGranted(permission: String): Boolean =
   ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
