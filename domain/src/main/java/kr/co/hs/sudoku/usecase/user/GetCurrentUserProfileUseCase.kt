package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCurrentUserProfileUseCase
@Inject constructor(
    private val repository: ProfileRepository
) : UseCase<Unit, ProfileEntity, GetCurrentUserProfileUseCase.Error>() {

    sealed interface Error
    object NotExistCurrentUser : Error

    class NotExistCurrentUserException(p0: String?) : Exception(p0)

    override fun invoke(
        param: Unit,
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ): Job = scope.launch {
        flow { emit(withContext(Dispatchers.IO) { getProfile() }) }
            .catch {
                when (it) {
                    is ProfileRepository.ProfileException -> when (it) {
                        is ProfileRepository.ProfileException.EmptyUserId ->
                            onResult(Result.Error(NotExistCurrentUser))

                        is ProfileRepository.ProfileException.ProfileNotFound ->
                            onResult(Result.Error(NotExistCurrentUser))
                    }

                    is NotExistCurrentUserException -> onResult(Result.Error(NotExistCurrentUser))
                    else -> onResult(Result.Exception(it))
                }
            }
            .collect { onResult(Result.Success(it)) }
    }

    operator fun invoke(
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ) = invoke(Unit, scope, onResult)

    private suspend fun getProfile() = with(repository) {
        runCatching {
            getProfile()
        }.getOrElse {
            when (it) {
                is NullPointerException ->
                    throw NotExistCurrentUserException("profile not found")

                is ProfileRepository.ProfileException -> when (it) {
                    is ProfileRepository.ProfileException.EmptyUserId ->
                        throw NotExistCurrentUserException("EmptyUserId")

                    is ProfileRepository.ProfileException.ProfileNotFound ->
                        throw NotExistCurrentUserException("ProfileNotFound")
                }

                else -> throw it
            }
        }
    }

    override suspend fun invoke(param: Unit, scope: CoroutineScope): ProfileEntity =
        scope.async { withContext(Dispatchers.IO) { getProfile() } }.await()

    suspend operator fun invoke(scope: CoroutineScope) = invoke(Unit, scope)

}