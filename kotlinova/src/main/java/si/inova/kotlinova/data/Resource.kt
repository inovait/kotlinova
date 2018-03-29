package si.inova.kotlinova.data

/**
 * @author Matej Drobnic
 */
sealed class Resource<T> {
    class Cancelled<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val exception: Throwable) : Resource<T>()
    data class Loading<T>(val data: T? = null) : Resource<T>()
}

val <T> Resource<T>.value: T?
    get() = when {
        this is Resource.Success -> data
        this is Resource.Loading -> data
        else -> null
    }