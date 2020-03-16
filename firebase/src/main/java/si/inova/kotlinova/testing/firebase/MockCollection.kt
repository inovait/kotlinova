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
import com.google.firebase.firestore.*
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.mockito.Answers

/**
 * @author Matej Drobnic
 */
class MockCollection<T>() {
    constructor(data: List<Pair<String, T>>) : this() {
        data.forEach {
            insertObject(it.first, it.second)
        }
    }

    var nextId: String = "NextID"

    private val documents = LinkedHashMap<String, DocumentReference>()
    private val listeners = ArrayList<EventListener<QuerySnapshot>>()

    fun createDocument(name: String): MockDocument<T> {
        val document = MockDocument<T>(name)
        insert(name, document.toDocumentRef())
        return document
    }

    fun insert(name: String, collection: DocumentReference) {
        documents[name] = collection
    }

    fun insertObject(name: String, data: T) {
        createDocument(name)
                .readValue = data

        updateListeners()
    }

    fun insertMap(name: String, data: Map<String, Any>) {
        createDocument(name)
                .readMap = data

        updateListeners()
    }

    fun clear() {
        documents.clear()

        updateListeners()
    }

    fun updateListeners() {
        val newSnapshot = toQuerySnapshot()

        for (listener in listeners) {
            listener.onEvent(newSnapshot, null)
        }
    }

    fun toQuerySnapshot(): QuerySnapshot {
        val documents = LinkedHashMap<String, DocumentReference>().apply {
            putAll(this@MockCollection.documents)
        }

        return mock {
            whenever(it.size()).thenReturn(documents.size)

            whenever(it.documents).then {
                documents
                        .values
                        .toList()
                        .map {
                            it.get().result
                        }
            }

            whenever(it.iterator()).then {
                documents
                        .values
                        .toList()
                        .map {
                            it.get().result
                        }
                        .iterator()
            }
        }
    }

    fun toColRef(): CollectionReference {
        return mock(defaultAnswer = Answers.RETURNS_SELF) {
            whenever(it.document()).then {
                val name = nextId

                documents[name]
                        ?: MockDocument<T>(name).toDocumentRef()
            }

            whenever(it.document(any())).then {
                val name = it.arguments[0] as String

                documents[name]
                        ?: MockDocument<T>(name).toDocumentRef()
            }

            whenever(it.get()).thenAnswer { (Tasks.forResult(toQuerySnapshot())) }

            whenever(it.addSnapshotListener(any())).thenAnswer {
                @Suppress("UNCHECKED_CAST")
                val listener = it.arguments[0] as EventListener<QuerySnapshot>
                listener.onEvent(toQuerySnapshot(), null)

                listeners.add(listener)

                val registration: ListenerRegistration = mock()
                doAnswer { listeners.remove(listener) }.whenever(registration).remove()
                registration
            }
        }
    }
}