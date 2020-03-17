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