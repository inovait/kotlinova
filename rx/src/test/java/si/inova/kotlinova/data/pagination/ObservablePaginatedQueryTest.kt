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