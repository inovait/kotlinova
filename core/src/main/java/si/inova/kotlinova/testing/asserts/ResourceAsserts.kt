@file:JvmName("ResourceAsserts")

package si.inova.kotlinova.testing.asserts

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import si.inova.kotlinova.data.resources.Resource
import si.inova.kotlinova.data.resources.value

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isSuccess(): ObjectAssert<Resource.Success<T>> {
    satisfies { input ->
        if (input is Resource.Error<*>) {
            throw AssertionError(
                "Result should be Resource.Success, but is Error",
                input.exception
            )
        }
    }

    isInstanceOf(Resource.Success::class.java)
    return assertThat(this.actualValue as Resource.Success<T>)
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.assertThatValue(): ObjectAssert<T?> {
    @Suppress("UNCHECKED_CAST")
    return assertThat(this.actualValue.value)
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isLoading(): ObjectAssert<Resource.Loading<T>> {
    satisfies { input ->
        if (input is Resource.Error<*>) {
            throw AssertionError(
                "Result should be Resource.Success, but is Error",
                input.exception
            )
        }
    }

    isInstanceOf(Resource.Loading::class.java)
    return assertThat(this.actualValue as Resource.Loading<T>)
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isSuccessWithValue(value: T): S {
    isSuccess()

    return satisfies {
        @Suppress("UNCHECKED_CAST")
        it as Resource.Success<T>

        assertThat(it.data).describedAs("Success Resource's data").isEqualTo(value)
    }
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isLoadingWithValue(value: T): S {
    isLoading()

    return satisfies {
        @Suppress("UNCHECKED_CAST")
        it as Resource.Loading<T>

        assertThat(it.data).describedAs("Success Resource's data").isEqualTo(value)
    }
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isErrorSatisfying(requirements: (Throwable) -> Unit): S {
    return satisfies { input ->
        isInstanceOf(Resource.Error::class.java)
        input as Resource.Error<*>

        requirements(input.exception)
    }
}

fun <S : AbstractAssert<S, out Resource<T>>, T>
    AbstractAssert<S, out Resource<T>>.isError(errorClass: Class<out Throwable>): S {
    return isErrorSatisfying {
        assertThat(it).isInstanceOf(errorClass)
    }
}