@file:JvmName("FirebaseUtils")

package si.inova.kotlinova.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import org.threeten.bp.Instant

/**
 * @author Matej Drobnic
 */

/**
 * @return value of the [DocumentSnapshot] if it exists in the database or *null* if it doesn't.
 */
inline fun <reified T> DocumentSnapshot.getOrNull(): T? {
    if (!exists()) {
        return null
    }

    return toObject(T::class.java)
}

/**
 * Convert Firestore timestamp to Instant
 */
fun Timestamp.toInstant(): Instant {
    return Instant.ofEpochSecond(seconds, nanoseconds.toLong())
}