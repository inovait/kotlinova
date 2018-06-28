@file:JvmName("FirebaseUtils")

package si.inova.kotlinova.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import org.threeten.bp.Instant
import si.inova.kotlinova.coroutines.await

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
 * Return [DocumentSnapshot] from cache first or from internet if cache is not available
 */
suspend fun DocumentReference.getFromCacheFirst(): DocumentSnapshot {
    return try {
        get(Source.CACHE).await()
    } catch (e: FirebaseFirestoreException) {
        get(Source.SERVER).await()
    }
}

/**
 * Convert Firestore timestamp to Instant
 */
fun Timestamp.toInstant(): Instant {
    return Instant.ofEpochSecond(seconds, nanoseconds.toLong())
}