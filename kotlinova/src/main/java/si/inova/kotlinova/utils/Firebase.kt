@file:JvmName("FirebaseUtils")

package si.inova.kotlinova.utils

import com.google.firebase.firestore.DocumentSnapshot

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