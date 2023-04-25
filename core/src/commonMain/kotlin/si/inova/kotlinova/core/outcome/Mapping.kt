/*
 * Copyright 2023 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 *  is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package si.inova.kotlinova.core.outcome

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Map data of this outcome, while keeping the type.
 *
 * If provided Outcome has no data, [mapper] never gets called.
 */
fun <A, B> Outcome<A>.mapData(mapper: (A) -> B): Outcome<B> {
   return when (this) {
      is Outcome.Error -> Outcome.Error(exception, data?.let { mapper(it) })
      is Outcome.Progress -> Outcome.Progress(data?.let { mapper(it) }, progress, style)
      is Outcome.Success -> Outcome.Success(mapper(data))
   }
}

/**
 * Map data of this outcome, while keeping the type.
 *
 * If provided Outcome has no data, [mapper] gets called with null data.
 */
fun <A, B> Outcome<A>.mapNullableData(mapper: (A?) -> B): Outcome<B> {
   return when (this) {
      is Outcome.Error -> Outcome.Error(exception, mapper(data))
      is Outcome.Progress -> Outcome.Progress(mapper(data), progress, style)
      is Outcome.Success -> Outcome.Success(mapper(data))
   }
}

/**
 * Map data of this outcome, while keeping the type, using suspend [mapper].
 *
 * If provided Outcome has no data, [mapper] never gets called.
 */
suspend fun <A, B> Outcome<A>.mapDataSuspend(mapper: suspend (A) -> B): Outcome<B> {
   return when (this) {
      is Outcome.Error -> Outcome.Error(exception, data?.let { mapper(it) })
      is Outcome.Progress -> Outcome.Progress(data?.let { mapper(it) }, progress, style)
      is Outcome.Success -> Outcome.Success(mapper(data))
   }
}

/**
 * Map data of this outcome, while keeping the type, using suspend [mapper].
 *
 * If provided Outcome has no data, [mapper] gets called with null data.
 */
suspend fun <A, B> Outcome<A>.mapNullableDataSuspend(mapper: suspend (A?) -> B): Outcome<B> {
   return when (this) {
      is Outcome.Error -> Outcome.Error(exception, mapper(data))
      is Outcome.Progress -> Outcome.Progress(mapper(data), progress, style)
      is Outcome.Success -> Outcome.Success(mapper(data))
   }
}

/**
 * Returns a flow that switches to a new flow produced by transform function every time the original flow emits a value.
 * When the original flow emits a new value, the previous flow produced by transform block is cancelled.
 *
 * Unlike regular flatMapLatest, this one will attempt to properly merge outcome types. It will always take the worst outcome
 * type from the two (upstream and the one provided by the transform function):
 * * If any of the outcomes is [Outcome.Error], [Outcome.Error] is returned, with the data of [this].
 * * If any of the outcomes is [Outcome.Progress], [Outcome.Progress] is returned, with the data of [this].
 * * Otherwise, [Outcome.Success] is returned, with the data of [this].
 */
fun <A, B> Flow<Outcome<A>>.flatMapLatestOutcome(mapper: (A) -> Flow<Outcome<B>>): Flow<Outcome<B>> {
   return flatMapLatest { upstreamOutcome ->
      val data = upstreamOutcome.data
      if (data == null) {
         @Suppress("UNCHECKED_CAST")
         return@flatMapLatest flowOf(upstreamOutcome as Outcome<B>)
      }

      val targetFlow = mapper(data)

      targetFlow.map { targetOutcome ->
         targetOutcome.downgradeTo(upstreamOutcome)
      }
   }
}

/**
 * Downgrade this outcome to the worst of the two:
 *
 * * If any of the outcomes is [Outcome.Error], [Outcome.Error] is returned, with the data of [this].
 * * If any of the outcomes is [Outcome.Progress], [Outcome.Progress] is returned, with the data of [this].
 * * Otherwise, [Outcome.Success] is returned, with the data of [this].
 */
fun <T> Outcome<T>.downgradeTo(
   targetType: Outcome<*>
): Outcome<T> {
   return when {
      this is Outcome.Error -> this
      targetType is Outcome.Error -> Outcome.Error(targetType.exception, data)
      this is Outcome.Progress -> {
         if (targetType is Outcome.Progress) {
            val combinedProgress = targetType.progress?.let { progress?.times(it) }
            val style = if (targetType.style == LoadingStyle.ADDITIONAL_DATA || this.style == LoadingStyle.ADDITIONAL_DATA) {
               LoadingStyle.ADDITIONAL_DATA
            } else {
               LoadingStyle.NORMAL
            }
            Outcome.Progress(data, combinedProgress, style)
         } else {
            this
         }
      }

      targetType is Outcome.Progress -> Outcome.Progress(data, targetType.progress, targetType.style)
      else -> this
   }
}

/**
 * Updates the data of the Outcome inside the [MutableStateFlow.value]
 * atomically using the specified [function] of its value.
 *
 * [function] may be evaluated multiple times, if value is being concurrently updated.
 *
 * If provided Outcome has no data, [function] never gets called.
 */

fun <T> MutableStateFlow<Outcome<T>>.updateData(function: (T) -> T) {
   return update { outcome ->
      outcome.mapData(function)
   }
}

/**
 * Updates the data of the Outcome inside the [MutableStateFlow.value]
 * atomically using the specified [function] of its value.
 *
 * [function] may be evaluated multiple times, if value is being concurrently updated.
 *
 * If provided Outcome has no data, [function] gets called with null data.
 */

fun <T> MutableStateFlow<Outcome<T>>.updateNullableData(function: (T?) -> T) {
   return update { outcome ->
      outcome.mapNullableData(function)
   }
}
