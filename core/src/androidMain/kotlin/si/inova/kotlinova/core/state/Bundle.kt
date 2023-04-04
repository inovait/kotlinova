package si.inova.kotlinova.core.state

import android.os.Binder
import android.os.Bundle
import android.os.Parcelable
import androidx.core.app.BundleCompat
import java.io.Serializable

/**
 * Helper single method to set any object into bundle without using different set methods for different types
 */
operator fun Bundle.set(key: String, value: Any) {
   when (value) {
      is String -> putString(key, value)
      is Int -> putInt(key, value)
      is Short -> putShort(key, value)
      is Long -> putLong(key, value)
      is Byte -> putByte(key, value)
      is ByteArray -> putByteArray(key, value)
      is Char -> putChar(key, value)
      is CharArray -> putCharArray(key, value)
      is CharSequence -> putCharSequence(key, value)
      is Float -> putFloat(key, value)
      is Bundle -> putBundle(key, value)
      is Binder -> BundleCompat.putBinder(this, key, value)
      is Parcelable -> putParcelable(key, value)
      is Serializable -> putSerializable(key, value)
      else -> error(
         "Type ${value.javaClass.canonicalName} of property $key is not supported in bundle"
      )
   }
}

/**
 * Convert bundle to regular map. Mostly useful for logging.
 */
fun Bundle.asMap(): Map<String, Any?> {
   return keySet().associateWith {
      @Suppress("DEPRECATION") // We cannot use type safe APIs in this case
      get(it)
   }
}
