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
) : UseCaseFlow<ClearChallengeUseCase.Param, Unit, ClearChallengeUseCase.Error>() {

    data class Param(val challenge: ChallengeEntity, val stage: Stage)

    sealed interface Error
    object SignInFirst : Error
    object StageClearFirst : Error

    override fun invoke(
        param: Param
    ): Flow<Result<Unit, Error>> = flow {
        if (!param.stage.isSudokuClear())
            throw ClearChallengeException.StageNotClearedException("")

        val record = param.stage.getClearTime()
        if (record < 0)
            throw ClearChallengeException.StageNotClearedException("invalid clear time : $record")

        emit(param)
    }.map {
        repository.putRecord(param.challenge.challengeId, param.stage.getClearTime())
    }.filter {
        it
    }.onEach {
        createHistory(param.challenge, param.stage.getClearTime()).first()
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        Result.Success(Unit) as Result<Unit, Error>
    }.catch {
        when (it) {
            is ChallengeRepository.ChallengeException -> when (it) {
                is ChallengeRepository.ChallengeException.RequiredCurrentUserException ->
                    emit(Result.Error(SignInFirst))
            }

            is ClearChallengeException -> when (it) {
                is ClearChallengeException.StageNotClearedException ->
                    emit(Result.Error(StageClearFirst))
            }

            else -> emit(Result.Exception(it))
        }
    }

    sealed class ClearChallengeException(p0: String?) : Exception(p0) {
        class StageNotClearedException(p0: String?) : ClearChallengeException(p0)
    }
}