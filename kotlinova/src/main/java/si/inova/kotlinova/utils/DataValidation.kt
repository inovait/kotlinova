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
inline fun <reified T> castToList(input: Any?): List<T>? {
    if (input == null) {
        return null
    }

    val list = input as List<*>

    for (element in list) {
        element as T
    }

    @Suppress("UNCHECKED_CAST")
    return list as List<T>
}

/**
 * Method that checks the nullability of the [input] and then safely cast it to the [T]
 *
 * @param fieldName Name of the field that is being validated. Used in error messages.
 *
 * @throws InvalidDataFormatException if [input] is null or if [input] is not of type [T]
 */
@Throws(InvalidDataFormatException::class)
inline fun <reified T> checkNullAndSafeCast(input: Any?, fieldName: String): T {
    if (input == null) {
        throw InvalidDataFormatException("Missing field $fieldName")
    }

    try {
        return input as T
    } catch (_: ClassCastException) {
        throw InvalidDataFormatException(
            "$fieldName should be ${T::class.java.name}, " +
                "but is ${input::class.java.name}"
        )
    }
}

/**
 * Method that safely casts [input] into [T] if [input] is not *null* or returns *null* otherwise
 *
 * @param fieldName Name of the field that is being validated. Used in error messages.
 *
 * @throws InvalidDataFormatException if [input] is not of type [T]
 */
@Throws(InvalidDataFormatException::class)
inline fun <reified T> safeCastNull(input: Any?, fieldName: String): T? {
    if (input == null) {
        return null
    }

    try {
        return input as T
    } catch (_: ClassCastException) {
        throw InvalidDataFormatException(
            "$fieldName should be ${T::class.java.name}, " +
                "but is ${input::class.java.name}"
        )
    }
}