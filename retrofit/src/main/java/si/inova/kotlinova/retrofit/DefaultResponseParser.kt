package si.inova.kotlinova.retrofit

import retrofit2.HttpException
import retrofit2.Response

object DefaultResponseParser : CoroutineCallAdapterFactory.ResponseParser {
    override fun <T> parseResponse(response: Response<T>): T {
        @Suppress("UNCHECKED_CAST")
        return if (response.isSuccessful) {
            // Assume that empty response requires
            response.body() ?: Unit as T
        } else {
            throw HttpException(response)
        }
    }
}