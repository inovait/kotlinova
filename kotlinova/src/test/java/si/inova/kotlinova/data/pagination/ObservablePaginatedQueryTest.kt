package si.inova.kotlinova.data.pagination

import org.junit.Assert.assertEquals
import org.junit.Test
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.testing.getAll

/**
 * @author Matej Drobnic
 */
class ObservablePaginatedQueryTest {
    @Test
    fun mutateRaw() {
        val data = listOf(
            1,
            2,
            3,
            4,
            5
        )

        val expected = listOf(
            "2",
            "6",
            "10"
        )

        val initialQuery = SinglePageObservablePaginatedQuery<Int>(data)

        @Suppress("USELESS_CAST")
        val mutated = initialQuery.mutateRaw {
            map { resource ->
                if (resource is Resource.Success) {
                    Resource.Success(resource.data
                        .filterNot { it % 2 == 0 }
                        .map { it * 2 }
                        .map { it.toString() }) as Resource<List<String>>
                } else {
                    throw Exception()
                }
            }
        }

        assertEquals(expected, mutated.getAll())
    }

    @Test
    fun mutate() {
        val data = listOf(
            5,
            10,
            15,
            20
        )

        val expected = listOf(
            "1T",
            "2T",
            "3T"
        )

        val initialQuery = SinglePageObservablePaginatedQuery(data)

        val mutated = initialQuery.mutate { list ->
            list.filter { it < 20 }
                .map { it / 5 }
                .map { "${it}T" }
        }

        assertEquals(expected, mutated.getAll())
    }

    @Test
    fun map() {
        val data = listOf(
            2,
            4,
            6,
            8
        )

        val expected = listOf(
            3,
            5,
            7,
            9
        )

        val initialQuery = SinglePageObservablePaginatedQuery(data)

        val mutated = initialQuery.map { it + 1 }

        assertEquals(expected, mutated.getAll())
    }
}