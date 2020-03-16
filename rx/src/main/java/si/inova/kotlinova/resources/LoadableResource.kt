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

package si.inova.kotlinova.resources

import io.reactivex.Flowable
import si.inova.kotlinova.data.resources.Resource

/**
 * Loadable resource that can change data mid-loading.
 * */
interface LoadableResource<T> {
    suspend fun load()
    val data: Flowable<Resource<T>>
}

inline fun <T, R> LoadableResource<T>.map(crossinline mapper: (Resource<T>) -> Resource<R>):
        LoadableResource<R> {
    val original = this

    return object : LoadableResource<R> {
        override suspend fun load() {
            original.load()
        }

        override val data: Flowable<Resource<R>>
            get() = original.data.map { mapper(it) }
    }
}