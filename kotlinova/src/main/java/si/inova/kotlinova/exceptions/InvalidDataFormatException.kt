package si.inova.kotlinova.exceptions

/**
 * Exception that gets thrown when received data is in unknown format
 *
 * @author Matej Drobnic
 */
class InvalidDataFormatException(message: String? = null) : Exception(message)