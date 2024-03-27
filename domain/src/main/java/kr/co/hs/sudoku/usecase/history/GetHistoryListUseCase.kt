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
import kr.co.hs.sudoku.repository.history.MyHistoryRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetHistoryListUseCase
@Inject constructor(
    private val historyRepository: HistoryRepository,
    private val myHistoryRepository: MyHistoryRepository
) {

    sealed interface Error
    object SignInFirst : Error
    object RequiredUserId : Error

    operator fun invoke(
        uid: String,
        date: Date,
        pageSize: Long
    ): Flow<UseCaseFlow.Result<HistoryEntity, Error>> = flow {
        historyRepository.getList(uid, date, pageSize).forEach { emit(it) }
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        UseCaseFlow.Result.Success(it) as UseCaseFlow.Result<HistoryEntity, Error>
    }.catch {
        when (it) {
            is RepositoryException -> when (it) {
                is RepositoryException.CorruptedException ->
                    emit(UseCaseFlow.Result.Exception(it))

                is RepositoryException.EmptyIdException ->
                    emit(UseCaseFlow.Result.Error(RequiredUserId))

                is RepositoryException.NotFoundException ->
                    emit(UseCaseFlow.Result.Exception(it))

                is RepositoryException.InvalidateParameterException ->
                    emit(UseCaseFlow.Result.Exception(it))
            }
        }
    }

    operator fun invoke(
        date: Date,
        pageSize: Long
    ): Flow<UseCaseFlow.Result<HistoryEntity, Error>> = flow {
        myHistoryRepository.getList(date, pageSize).forEach { emit(it) }
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        UseCaseFlow.Result.Success(it) as UseCaseFlow.Result<HistoryEntity, Error>
    }.catch {
        when (it) {
            is MyHistoryRepository.MyHistoryException -> when (it) {
                is MyHistoryRepository.MyHistoryException.NotMineException ->
                    emit(UseCaseFlow.Result.Exception(it))

                is MyHistoryRepository.MyHistoryException.RequiredCurrentUserException ->
                    emit(UseCaseFlow.Result.Error(SignInFirst))
            }

            else -> emit(UseCaseFlow.Result.Exception(it))
        }
    }

}