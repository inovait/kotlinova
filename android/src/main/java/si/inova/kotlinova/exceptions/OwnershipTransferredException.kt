package si.inova.kotlinova.exceptions

import kotlinx.coroutines.experimental.CancellationException

/**
 * Exception that gets triggered whenever another coroutine takes ownership of
 * the [si.inova.kotlinova.data.Resource] object from us
 *
 * @author Matej Drobnic
 */
class OwnershipTransferredException : CancellationException()