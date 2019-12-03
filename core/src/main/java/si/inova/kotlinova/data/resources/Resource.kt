package si.inova.kotlinova.data.resources

/**
 * @author Matej Drobnic
 */
sealed class Resource<T> {
    class Cancelled<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val exception: Throwable) : Resource<T>()
    data class Loading<T>(val data: T? = null, val progress: Float? = null) : Resource<T>()
}

val <T> Resource<T>.value: T?
    get() = when {
        this is Resource.Success -> data
        this is Resource.Loading -> data
        else -> null
    }

/**
 * Apply function on data of this Resource (if it contains any data)
 */
inline fun <I, O> Resource<I>.mapData(mapper: (I) -> O): Resource<O> {
    return when (this) {
        is Resource.Success -> Resource.Success(
            mapper(data)
        )
        is Resource.Error -> Resource.Error(
            exception
        )
        is Resource.Cancelled -> Resource.Cancelled()
        is Resource.Loading -> {
            val newData = data?.let(mapper)
            return Resource.Loading(newData)
        }
    }
}

/**
 * Returns resource's value or throws an exception if resource contains an exception.
 *
 * This method only supports [Resource.Success] and [Resource.Error]
 */
fun <T> Resource<T>.unwrap(): T {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> throw exception
        else -> error("Resource.unwrap() only supports final resource (Success or Error)")
    }
}