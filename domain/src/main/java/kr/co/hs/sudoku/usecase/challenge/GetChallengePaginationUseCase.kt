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
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChallengePaginationUseCase
@Inject constructor(
    private val repository: ChallengeRepository
) {

    sealed interface Error
    object SignInFirst : Error

    operator fun invoke(
        pageSize: Long
    ): Flow<UseCaseFlow.Result<ChallengeEntity, Error>> = invoke(Date(), pageSize)

    operator fun invoke(
        date: Date,
        pageSize: Long
    ): Flow<UseCaseFlow.Result<ChallengeEntity, Error>> = flow {
        emitAll(repository.getChallenges(date, pageSize).asFlow())
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        UseCaseFlow.Result.Success(it) as UseCaseFlow.Result<ChallengeEntity, Error>
    }.catch {
        when (it) {
            is ChallengeRepository.ChallengeException -> when (it) {
                is ChallengeRepository.ChallengeException.RequiredCurrentUserException ->
                    emit(UseCaseFlow.Result.Error(SignInFirst))
            }

            else -> emit(UseCaseFlow.Result.Exception(it))
        }
    }
}