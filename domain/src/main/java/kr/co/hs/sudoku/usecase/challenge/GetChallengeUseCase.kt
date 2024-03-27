package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.RepositoryException
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.usecase.UseCaseFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChallengeUseCase
@Inject constructor(
    private val repository: ChallengeRepository
) : UseCaseFlow<String, ChallengeEntity, GetChallengeUseCase.Error>() {

    sealed interface Error
    object ChallengeNotFound : Error
    object SignInFirst : Error

    override fun invoke(
        param: String
    ): Flow<Result<ChallengeEntity, Error>> = flow {
        emit(repository.getChallenge(param))
    }.flowOn(
        Dispatchers.IO
    ).map {
        @Suppress("USELESS_CAST")
        Result.Success(it) as Result<ChallengeEntity, Error>
    }.catch {
        when (it) {
            is ChallengeRepository.ChallengeException -> when (it) {
                is ChallengeRepository.ChallengeException.RequiredCurrentUserException ->
                    emit(Result.Error(SignInFirst))
            }

            is RepositoryException -> when (it) {
                is RepositoryException.NotFoundException -> emit(Result.Error(ChallengeNotFound))
                else -> emit(Result.Exception(it))
            }

            else -> emit(Result.Exception(it))
        }
    }
}