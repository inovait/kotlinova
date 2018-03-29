package si.inova.kotlinova.testing.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
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

                collections[name] ?: throw IllegalArgumentException("Collection does not exist")
            }
        }
    }
}