package kr.co.hs.sudoku.usecase.abs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class UseCase<P, R, E> {

    abstract operator fun invoke(
        param: P,
        scope: CoroutineScope,
        onResult: (Result<R, E>) -> Unit
    ): Job

    abstract suspend operator fun invoke(param: P, scope: CoroutineScope): R

    sealed class Result<out T, out E> {
        data class Success<T>(val data: T) : Result<T, Nothing>()
        data class Error<E>(val e: E) : Result<Nothing, E>()
        data class Exception(val t: Throwable) : Result<Nothing, Nothing>()
    }
}