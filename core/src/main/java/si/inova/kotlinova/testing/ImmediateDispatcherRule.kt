package si.inova.kotlinova.testing

import kotlinx.coroutines.Dispatchers

/**
 * Rule that converts all thread dispatchers into immediate execution ones.
 *
 * @author Matej Drobnic
 */
class ImmediateDispatcherRule : DispatcherReplacementRule(Dispatchers.Unconfined)