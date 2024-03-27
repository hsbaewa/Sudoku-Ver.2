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
class StartChallengeUseCase
@Inject constructor(
    private val repository: ChallengeRepository
) : UseCaseFlow<ChallengeEntity, Unit, StartChallengeUseCase.Error>() {

    sealed interface Error
    object AlreadyClear : Error
    object SignInFirst : Error

    override fun invoke(
        param: ChallengeEntity
    ): Flow<Result<Unit, Error>> = flow {
        if (param.isComplete)
            throw StartChallengeException.AlreadyClearException("")

        repository.putReserveRecord(param.challengeId)
        emit(Unit)
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        Result.Success(it) as Result<Unit, Error>
    }.catch {
        when (it) {
            is ChallengeRepository.ChallengeException -> when (it) {
                is ChallengeRepository.ChallengeException.RequiredCurrentUserException ->
                    emit(Result.Error(SignInFirst))
            }

            is StartChallengeException.AlreadyClearException -> emit(Result.Error(AlreadyClear))
            else -> emit(Result.Exception(it))
        }
    }

    sealed class StartChallengeException(p0: String?) : Exception(p0) {
        class AlreadyClearException(p0: String?) : StartChallengeException(p0)
    }
}