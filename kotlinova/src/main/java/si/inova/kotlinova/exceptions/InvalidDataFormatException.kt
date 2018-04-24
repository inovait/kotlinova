package si.inova.kotlinova.exceptions

/**
 * Exception that gets thrown when received data is in unknown format
 *
 * @author Matej Drobnic
 */
class InvalidDataFormatException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
}