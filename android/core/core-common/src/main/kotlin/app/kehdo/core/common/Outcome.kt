package app.kehdo.core.common

/**
 * Outcome of an operation that can succeed or fail.
 * Used everywhere instead of exceptions for predictable control flow.
 */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: KehdoError) : Outcome<Nothing>

    fun <R> map(transform: (T) -> R): Outcome<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun onSuccess(block: (T) -> Unit): Outcome<T> {
        if (this is Success) block(value)
        return this
    }

    inline fun onFailure(block: (KehdoError) -> Unit): Outcome<T> {
        if (this is Failure) block(error)
        return this
    }

    companion object {
        fun <T> success(value: T): Outcome<T> = Success(value)
        fun failure(error: KehdoError): Outcome<Nothing> = Failure(error)
    }
}
