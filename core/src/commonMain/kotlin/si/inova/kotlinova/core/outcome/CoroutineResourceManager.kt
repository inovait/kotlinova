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

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.flow.collectInto
import si.inova.kotlinova.core.reporting.ErrorReporter
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Utility class that manages lifecycle of multiple coroutines that manage the same resource.
 */
open class CoroutineResourceManager(
   val scope: CoroutineScope,
   private val reportService: ErrorReporter
) {
   private val activeJobs = ConcurrentHashMap<Any, Job>()

   /**
    * Method that launches coroutine task that handles data fetching for provided resources.
    *
    * This method:
    *
    * 1. Cancels any previous coroutines using this resource to prevent clashing
    * 2. Automatically puts the outcome into loading state with previous data as partial data
    *    (this data can be overridden using [currentValue] parameter)
    * 3. Calls your provided task
    * 4. Automatically handles cancellation exceptions
    * 5. Automatically forwards thrown exceptions to the resource as [Outcome.Error], with original data being kept
    *    (unless [keepDataOnExceptions] is disabled)
    * 6. Automatically reports exceptions to the provided ReportService
    */
   open fun <T> launchResourceControlTask(
      resource: MutableStateFlow<Outcome<T>>,
      currentValue: T? = resource.value.data,
      context: CoroutineContext = EmptyCoroutineContext,
      keepDataOnExceptions: Boolean = true,
      block: suspend ResourceControlBlock<T>.() -> Unit
   ) = launchBoundControlTask(resource, context) {
      try {
         resource.value = Outcome.Progress(currentValue)

         block(ResourceControlBlock(resource, this))
      } catch (e: CancellationException) {
         throw e
      } catch (e: Exception) {
         val exception = if (e is CauseException) {
            e
         } else {
            UnknownCauseException(cause = e)
         }

         interceptException(e)

         resource.value = Outcome.Error(exception, resource.value.data.takeIf { keepDataOnExceptions })
      }
   }

   private fun interceptException(exception: Exception) {
      reportService.report(exception)
   }

   inner class ResourceControlBlock<T>(
      originalFlow: MutableStateFlow<Outcome<T>>,
      coroutineScope: CoroutineScope
   ) : MutableStateFlow<Outcome<T>> by originalFlow,
      CoroutineScope by coroutineScope {
      fun launchAndEmitAll(flow: Flow<Outcome<T>>) {
         launch {
            emitAll(flow)
         }
      }

      suspend fun emitAll(flow: Flow<Outcome<T>>) {
         val interceptedFlow = flow.onEach {
            reportError(it)
         }

         interceptedFlow.collectInto(this)
      }
   }

   /**
    * Launch new coroutine, bound to the passed resource object.
    *
    * Any previous launched coroutines that were bound to an object equal to passed object will
    * be cancelled.
    */
   @Synchronized
   fun launchBoundControlTask(
      resource: Any,
      context: CoroutineContext = EmptyCoroutineContext,
      block: suspend CoroutineScope.() -> Unit
   ) {
      // To prevent threading issues, only one job can handle one resource at a time
      // Cancel active job first.
      val currentJobForThatResource = activeJobs.remove(resource)

      currentJobForThatResource?.cancel()

      val newJob = scope.launch(context, CoroutineStart.LAZY) {
         val thisJob = coroutineContext.job

         currentJobForThatResource?.join()

         if (!isActive) {
            return@launch
         }

         try {
            block()
         } finally {
            activeJobs.remove(resource, thisJob)
         }
      }

      activeJobs[resource] = newJob

      newJob.start()
   }

   /**
    * If passed [outcome] is a [Outcome.Error], it will report it to the internal error reporter.
    */
   fun <T> reportError(outcome: Outcome<T>) {
      if (outcome is Outcome.Error) {
         interceptException(outcome.exception)
      }
   }

   /**
    * @return whether resource is already being managed by a Job
    */
   fun isResourceTaken(resource: Any): Boolean {
      return activeJobs.containsKey(resource)
   }

   /**
    * Cancel job that currently manages passed resource.
    */
   fun cancelResource(resource: Any) {
      val activeJob = activeJobs[resource]
      activeJob?.cancel()
   }

   /**
    * Get the job that currently manages passed resource.
    * This is only meant for read-only inspection. Do
    * NOT cancel the job. Use [cancelResource] method instead.
    */
   fun getCurrentJob(resource: Any): Job? {
      return activeJobs[resource]
   }
}
