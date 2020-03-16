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

package si.inova.kotlinova.testing.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

/**
 * @author Matej Drobnic
 */
class MockFirestore {
    private val collections = HashMap<String, CollectionReference>()

    fun <T> createCollection(name: String): MockCollection<T> {
        val collection = MockCollection<T>()
        insert(name, collection.toColRef())
        return collection
    }

    fun insert(name: String, collection: CollectionReference) {
        collections[name] = collection
    }

    fun toFirestore(): FirebaseFirestore {
        return mock {
            whenever(it.collection(any())).then {
                val name = it.arguments[0] as String

                collections[name] ?: MockCollection<Any>().toColRef()
            }

            whenever(it.runTransaction<Any>(any())).thenAnswer {
                val transFunc = it.arguments[0] as Transaction.Function<*>
                val result = transFunc.apply(transaction)

                Tasks.forResult(result)
            }
        }
    }

    private val transaction: Transaction by lazy {
        mock<Transaction> {
            whenever(it.get(any())).then {
                val ref = it.arguments[0] as DocumentReference
                ref.get().result
            }

            whenever(it.delete(any())).then {
                val ref = it.arguments[0] as DocumentReference
                ref.delete()
                it.mock
            }

            whenever(it.set(any(), any<Map<String, Any>>())).then {
                val ref = it.arguments[0] as DocumentReference
                @Suppress("UNCHECKED_CAST")
                ref.set(it.arguments[1] as Map<String, Any>)
                it.mock
            }

            whenever(it.set(any(), any<Any>())).then {
                val ref = it.arguments[0] as DocumentReference
                ref.set(it.arguments[1] as Any)
                it.mock
            }

            whenever(it.update(any(), any<Map<String, Any>>())).then {
                val ref = it.arguments[0] as DocumentReference
                @Suppress("UNCHECKED_CAST")
                ref.update(it.arguments[1] as Map<String, Any>)
                it.mock
            }
        }
    }
}