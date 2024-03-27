package kr.co.hs.sudoku.usecase.history

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.history.HistoryEntity
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.repository.history.MyHistoryRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateHistoryUseCase @Inject constructor(
    private val repository: MyHistoryRepository
) {

    sealed interface Error
    object SignInFirst : Error

    operator fun invoke(
        challengeEntity: ChallengeEntity,
        record: Long
    ): Flow<UseCaseFlow.Result<HistoryEntity, Error>> = flow {
        emit(repository.createChallengeClearHistory(challengeEntity, record))
    }.flowOn(
        Dispatchers.IO
    ).asUseCaseFlow()

    operator fun invoke(
        battleEntity: BattleEntity,
        record: Long
    ): Flow<UseCaseFlow.Result<HistoryEntity, Error>> = flow {
        emit(repository.createBattleClearHistory(battleEntity, record))
    }.flowOn(
        Dispatchers.IO
    ).asUseCaseFlow()

    private fun Flow<HistoryEntity>.asUseCaseFlow() =
        map {
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

                is RepositoryException -> emit(UseCaseFlow.Result.Exception(it))
                else -> emit(UseCaseFlow.Result.Exception(it))
            }
        }
}