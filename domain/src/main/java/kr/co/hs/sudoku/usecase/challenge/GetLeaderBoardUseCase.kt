package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import javax.inject.Inject

class GetLeaderBoardUseCase
@Inject constructor(
    private val repository: ChallengeRepository
) : UseCaseFlow<ChallengeEntity, RankerEntity, GetLeaderBoardUseCase.Error>() {

    sealed interface Error
    object SignInFirst : Error

    override fun invoke(
        param: ChallengeEntity
    ): Flow<Result<RankerEntity, Error>> = flow {
        emitAll(repository.getRecords(param.challengeId).asFlow())
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        Result.Success(it) as Result<RankerEntity, Error>
    }.catch {
        when (it) {
            is ChallengeRepository.ChallengeException -> when (it) {
                is ChallengeRepository.ChallengeException.RequiredCurrentUserException ->
                    emit(Result.Error(SignInFirst))
            }

            else -> Result.Exception(it)
        }
    }
}