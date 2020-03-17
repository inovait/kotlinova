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

package si.inova.kotlinova.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import si.inova.kotlinova.time.AndroidTimeProvider

class DocumentMemoryCacheTest {
    private lateinit var documentMemoryCache: DocumentMemoryCache

    @Before
    fun setUp() {
        documentMemoryCache = DocumentMemoryCache(5_000)
    }

    @Test
    fun insertRetrieve() {
        documentMemoryCache[1] = "A"
        documentMemoryCache[2] = "B"

        assertEquals("A", documentMemoryCache[1])
        assertEquals("B", documentMemoryCache[2])
    }

    @Test
    fun evictAfterTime() {
        AndroidTimeProvider.elapsedRealtimeProvider = { 0 }
        documentMemoryCache[1] = "A"
        AndroidTimeProvider.elapsedRealtimeProvider = { 2000 }
        documentMemoryCache[2] = "B"

        AndroidTimeProvider.elapsedRealtimeProvider = { 6000 }

        assertNull(documentMemoryCache[1])
        assertEquals("B", documentMemoryCache[2])

        AndroidTimeProvider.elapsedRealtimeProvider = { 10000 }
        assertNull(documentMemoryCache[1])
        assertNull(documentMemoryCache[2])
    }

    @Test
    fun retrieveNoElement() {
        assertNull(documentMemoryCache[1])
        assertNull(documentMemoryCache[3])
        assertNull(documentMemoryCache[2])
    }

    @Suppress("UNUSED_VARIABLE")
    @Test(expected = ClassCastException::class)
    fun insertRetrieveInvalidData() {
        documentMemoryCache[1] = "A"

        val data = documentMemoryCache.get<Int>(1)
    }

    @Test
    fun manualEvict() {
        documentMemoryCache[1] = "A"
        documentMemoryCache[2] = "B"

        documentMemoryCache.evict(1)

        assertNull(documentMemoryCache[1])
        assertEquals("B", documentMemoryCache[2])
    }

    @Test
    fun getOrProduceSingle() {
        assertEquals("B", documentMemoryCache.getOrProduce(1) { "B" })

        assertEquals("B", documentMemoryCache[1])
    }

    @Test
    fun getOrProduceExisting() {
        documentMemoryCache[1] = "A"
        assertEquals("A", documentMemoryCache.getOrProduce(1) { fail() })

        assertEquals("A", documentMemoryCache[1])
    }

    @Test
    fun getOrProduceForce() {
        documentMemoryCache[1] = "A"
        assertEquals("B",
            documentMemoryCache.getOrProduce(1, forceProduce = true) { "B" })

        assertEquals("B", documentMemoryCache[1])
    }

    @Test
    fun getOrProduceAfterTime() {
        AndroidTimeProvider.elapsedRealtimeProvider = { 0 }
        documentMemoryCache[1] = "A"

        AndroidTimeProvider.elapsedRealtimeProvider = { 10000 }

        assertEquals("B",
            documentMemoryCache.getOrProduce(1) { "B" })

        assertEquals("B", documentMemoryCache[1])
    }

    @Test
    fun evictStale() {
        AndroidTimeProvider.elapsedRealtimeProvider = { 0 }
        documentMemoryCache[1] = "A"
        AndroidTimeProvider.elapsedRealtimeProvider = { 2000 }
        documentMemoryCache[2] = "B"

        assertEquals(2, documentMemoryCache.size)

        AndroidTimeProvider.elapsedRealtimeProvider = { 6000 }

        assertEquals(2, documentMemoryCache.size)
        documentMemoryCache.evictStaleEntries()
        assertEquals(1, documentMemoryCache.size)

        assertNull(documentMemoryCache[1])
        assertEquals("B", documentMemoryCache[2])
    }

    @Test
    fun clear() {
        AndroidTimeProvider.elapsedRealtimeProvider = { 0 }
        documentMemoryCache[1] = "A"
        AndroidTimeProvider.elapsedRealtimeProvider = { 2000 }
        documentMemoryCache[2] = "B"

        AndroidTimeProvider.elapsedRealtimeProvider = { 6000 }

        documentMemoryCache.clear()
        assertEquals(0, documentMemoryCache.size)

        assertNull(documentMemoryCache[1])
        assertNull(documentMemoryCache[2])
    }
}