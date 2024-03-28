package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kr.co.hs.sudoku.core.Stage
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import kr.co.hs.sudoku.usecase.history.CreateHistoryUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClearChallengeUseCase
@Inject constructor(
    private val repository: ChallengeRepository,
    private val createHistory: CreateHistoryUseCase
) {

    sealed interface Error
    object SignInFirst : Error
    object StageClearFirst : Error

    operator fun invoke(
        challenge: ChallengeEntity,
        stage: Stage
    ): Flow<UseCaseFlow.Result<Unit, Error>> =
        flow {
            if (!stage.isSudokuClear())
                throw ClearChallengeException.StageNotClearedException("")

            val record = stage.getClearTime()
            if (record < 0)
                throw ClearChallengeException.StageNotClearedException("invalid clear time : $record")

            emit(Unit)
        }.map {
            repository.putRecord(challenge.challengeId, stage.getClearTime())
        }.filter {
            it
        }.onEach {
            createHistory(challenge, stage.getClearTime()).first()
        }.flowOn(
            Dispatchers.IO
        ).map {
            @Suppress("USELESS_CAST")
            UseCaseFlow.Result.Success(Unit) as UseCaseFlow.Result<Unit, Error>
        }.catch {
            when (it) {
                is ChallengeRepository.ChallengeException -> when (it) {
                    is ChallengeRepository.ChallengeException.RequiredCurrentUserException ->
                        emit(UseCaseFlow.Result.Error(SignInFirst))
                }

                is ClearChallengeException -> when (it) {
                    is ClearChallengeException.StageNotClearedException ->
                        emit(UseCaseFlow.Result.Error(StageClearFirst))
                }

                else -> emit(UseCaseFlow.Result.Exception(it))
            }
        }

    sealed class ClearChallengeException(p0: String?) : Exception(p0) {
        class StageNotClearedException(p0: String?) : ClearChallengeException(p0)
    }
}