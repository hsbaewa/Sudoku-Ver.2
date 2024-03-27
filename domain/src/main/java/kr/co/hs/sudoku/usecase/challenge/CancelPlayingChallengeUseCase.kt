package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelPlayingChallengeUseCase
@Inject constructor(
    private val repository: ChallengeRepository,
) : UseCaseFlow<ChallengeEntity, Unit, CancelPlayingChallengeUseCase.Error>() {

    sealed interface Error
    object SignInFirst : Error

    override fun invoke(
        param: ChallengeEntity,
    ): Flow<Result<Unit, Error>> = flow {
        emit(repository.deleteRecord(param.challengeId))
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

            else -> emit(Result.Exception(it))
        }
    }
}