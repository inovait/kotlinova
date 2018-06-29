/**
 * @author Matej Drobnic
 */
@file:JvmName("FirebaseTestUtils")

package si.inova.kotlinova.testing

import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Assert

fun assertDocumentsEqual(expected: DocumentSnapshot, actual: DocumentSnapshot) {
    Assert.assertEquals(expected.id, actual.id)
    Assert
        .assertEquals(expected.toObject(Any::class.java), expected.toObject(Any::class.java))
}

fun assertDocumentsEqual(expected: List<DocumentSnapshot>, actual: List<DocumentSnapshot>) {
    Assert.assertEquals(expected.size, actual.size)

    for (elements in expected.zip(actual)) {
        assertDocumentsEqual(elements.first, elements.second)
    }
}