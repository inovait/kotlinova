/**
 * @author Matej Drobnic
 */
@file:JvmName("DataValidationUtils")

package si.inova.kotlinova.utils

import si.inova.kotlinova.exceptions.InvalidDataFormatException

/**
 * Helper wrapper function around any data validation code.
 *
 * Every exception thrown inside that block is wrapped into [InvalidDataFormatException].
 *
 * @throws InvalidDataFormatException
 */
@Throws(InvalidDataFormatException::class)
inline fun <T> validate(block: () -> T): T {
    try {
        return block()
    } catch (e: InvalidDataFormatException) {
        throw e
    } catch (e: Exception) {
        throw InvalidDataFormatException(e)
    }
}

/**
 * Safely cast [Any] object to [List]<[T]> object.
 *
 * @throws ClassCastException if any element in the list is not of type [T]
 */
@Throws(ClassCastException::class)
inline fun <reified T> castToList(input: Any): List<T> {
    val list = input as List<*>

    for (element in list) {
        element as T
    }

    @Suppress("UNCHECKED_CAST")
    return list as List<T>
}