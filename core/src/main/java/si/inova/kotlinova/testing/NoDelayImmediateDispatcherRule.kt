package si.inova.kotlinova.testing

import kotlinx.coroutines.delay

/**
 * Rule that converts all thread dispatchers into immediate execution ones while also
 * removing any [delay] calls.
 *
 * @author Matej Drobnic
 */
class NoDelayImmediateDispatcherRule : DispatcherReplacementRule(NoDelayUnconfinedDispatcher())