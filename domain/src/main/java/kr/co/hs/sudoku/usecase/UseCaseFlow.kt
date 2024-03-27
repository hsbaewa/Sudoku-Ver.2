package kr.co.hs.sudoku.usecase

import kotlinx.coroutines.flow.Flow

abstract class UseCaseFlow<P, R, E> {

    abstract operator fun invoke(param: P): Flow<Result<R, E>>

    sealed class Result<out T, out E> {
        data class Success<T>(val data: T) : Result<T, Nothing>()
        data class Error<E>(val e: E) : Result<Nothing, E>()
        data class Exception(val t: Throwable) : Result<Nothing, Nothing>()

        fun getSuccessData() = (this as? Success)?.data
    }
}