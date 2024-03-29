package kr.co.hs.sudoku.usecase

sealed interface UseCaseResult<out S, out F> {
    class Success<S>(val data: S) : UseCaseResult<S, Nothing>
    class Error<F : Throwable>(val error: F) : UseCaseResult<Nothing, F>
}