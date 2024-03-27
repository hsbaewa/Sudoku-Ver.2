package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteChallengeUseCase
@Inject constructor(
    private val repository: ChallengeRepository
) : NoErrorUseCase<ChallengeEntity, Unit>() {

    override fun invoke(
        param: ChallengeEntity
    ): Flow<Result<Unit>> = flow {
        emit(repository.removeChallenge(param.challengeId))
    }.flowOn(
        Dispatchers.IO
    ).map {
        Result.Success(Unit)
    }
}