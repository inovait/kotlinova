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

package si.inova.kotlinova.rx

import io.reactivex.Flowable
import kotlinx.coroutines.reactive.awaitFirst
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.unwrap

/**
 * Await first value from this resource stream (or throw exception if error happens)
 *
 * @exception NoSuchElementException if stream terminates before Error or Success resource
 *
 * @see si.inova.kotlinova.data.resources.Resource.unwrap
 */
suspend fun <T> Flowable<Resource<T>>.awaitFirstUnpacked(): T {
    return filter {
        it is Resource.Success || it is Resource.Error
    }
            .awaitFirst()
            .unwrap()
}
