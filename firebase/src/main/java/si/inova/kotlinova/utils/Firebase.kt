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