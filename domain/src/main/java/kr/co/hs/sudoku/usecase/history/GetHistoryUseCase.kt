package kr.co.hs.sudoku.usecase.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.repository.history.HistoryRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHistoryUseCase
@Inject constructor(
    private val repository: HistoryRepository
) : UseCaseFlow<String, HistoryEntity, GetHistoryUseCase.Error>() {

    sealed interface Error
    object NotFound : Error

    override fun invoke(
        param: String
    ): Flow<Result<HistoryEntity, Error>> = flow {
        emit(repository.get(param))
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        Result.Success(it) as Result<HistoryEntity, Error>
    }.catch {
        when (it) {
            is RepositoryException -> when (it) {
                is RepositoryException.CorruptedException ->
                    emit(Result.Exception(it))

                is RepositoryException.EmptyIdException ->
                    emit(Result.Exception(it))

                is RepositoryException.NotFoundException ->
                    emit(Result.Error(NotFound))

                is RepositoryException.InvalidateParameterException ->
                    emit(Result.Exception(it))
            }

            else -> emit(Result.Exception(it))
        }
    }
}