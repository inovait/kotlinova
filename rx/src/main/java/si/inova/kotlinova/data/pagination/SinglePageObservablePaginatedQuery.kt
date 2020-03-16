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

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import si.inova.kotlinova.data.resources.Resource

/**
 * [PaginatedQuery] that only provides single page (one list of items)
 *
 * @author Matej Drobnic
 */

class SinglePageObservablePaginatedQuery<T>(private val providedData: List<T>) :
        ObservablePaginatedQuery<T> {

    override val isAtEnd: Boolean = true

    private val _data = BehaviorSubject.create<Resource<List<T>>>()
    override val data: Flowable<Resource<List<T>>> = _data.toFlowable(BackpressureStrategy.LATEST)

    override suspend fun loadFirstPage() {
        _data.onNext(Resource.Success(providedData))
    }

    override suspend fun loadNextPage() = Unit
}