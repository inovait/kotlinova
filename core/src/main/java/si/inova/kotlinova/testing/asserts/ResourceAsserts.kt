@file:JvmName("ResourceAsserts")

package si.inova.kotlinova.testing.asserts

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import si.inova.kotlinova.data.resources.Resource

fun <S : AbstractAssert<S, in Resource<T>>, T> AbstractAssert<S, in Resource<T>>.isSuccess(): S {
    return satisfies { input ->
        if (input is Resource.Error<*>) {
            throw AssertionError(
                "Result should be Resource.Success, but is Error",
                input.exception
            )
        }

        isInstanceOf(Resource.Success::class.java)
    }
}

fun <S : AbstractAssert<S, in Resource<T>>, T>
    AbstractAssert<S, in Resource<T>>.isSuccessWithValue(value: T): S {
    isSuccess()

    return satisfies {
        @Suppress("UNCHECKED_CAST")
        it as Resource.Success<T>

        assertThat(it.data).describedAs("Success Resource's data").isEqualTo(value)
    }
}

fun <S : AbstractAssert<S, in Resource<T>>, T>
    AbstractAssert<S, in Resource<T>>.isErrorSatisfying(
    requirements: (Throwable) -> Unit
): S {
    return satisfies { input ->
        isInstanceOf(Resource.Error::class.java)
        input as Resource.Error<*>

        requirements(input.exception)
    }
}

fun <S : AbstractAssert<S, in Resource<T>>, T>
    AbstractAssert<S, in Resource<T>>.isError(
    errorClass: Class<out Throwable>
): S {
    return isErrorSatisfying {
        assertThat(it).isInstanceOf(errorClass)
    }
}