package si.inova.kotlinova.compose.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.flow.withBlinkingPrevention
import si.inova.kotlinova.core.outcome.Outcome

/**
 * Collect a flow in a way that it will ensure that [Outcome.Progress] is never
 * emitted for a very short amount of time, that would cause blinking on the screen. It achieves that by:
 *
 * * Not emitting [Outcome.Progress] unless progress takes at least [waitThisLongToShowLoadingMs] milliseconds.
 *   If [doNotWaitForInterimLoadings] is set to true, this will only apply to the initial loading, not to the subsequent
 *   loadings that happen after a refresh. Set this flag, when this flow is bound to Pull-to-refresh.
 * * Once [Outcome.Progress] will be emitted, it will not emit any non-progress
 *   state for at least [keepLoadingActiveForAtLeastMs] milliseconds
 *
 * Due to above behavior, initial state of this state will be *null*. In this case, do not display anything on your screen.
 *
 * @see collectAsStateWithLifecycle
 * @see withBlinkingPrevention
 */
@Composable
fun <T> Flow<Outcome<T>>.collectAsStateWithLifecycleAndBlinkingPrevention(
   waitThisLongToShowLoadingMs: Long = 100,
   keepLoadingActiveForAtLeastMs: Long = 500,
   doNotWaitForInterimLoadings: Boolean = false,
): State<Outcome<T>?> {
   val filteredFlow = remember(this) {
      this.withBlinkingPrevention(
         waitThisLongToShowLoadingMs,
         keepLoadingActiveForAtLeastMs,
         doNotWaitForInterimLoadings
      )
   }

   return filteredFlow.collectAsStateWithLifecycle(null)
}
