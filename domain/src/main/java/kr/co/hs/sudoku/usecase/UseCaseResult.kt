package kr.co.hs.sudoku.usecase

sealed interface UseCaseResult<out S, out F : Throwable> {
    class Success<S>(val data: S) : UseCaseResult<S, Nothing>
    class Error<F : Throwable>(val e: F) : UseCaseResult<Nothing, F>
}