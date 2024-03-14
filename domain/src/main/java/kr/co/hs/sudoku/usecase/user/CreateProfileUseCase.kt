package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.ProfileRepositoryQualifier
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileUseCase
@Inject constructor(
    @ProfileRepositoryQualifier private val repository: ProfileRepository
) : UseCase<ProfileEntity, ProfileEntity, CreateProfileUseCase.Error>() {

    sealed interface Error
    object AlreadyUser : Error
    object EmptyUserId : Error

    private class AlreadyUserException(p0: String?) : Exception(p0)

    override fun invoke(
        param: ProfileEntity,
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ): Job = scope.launch {
        flow { emit(withContext(Dispatchers.IO) { createProfile(param) }) }
            .catch {
                when (it) {
                    is ProfileRepository.ProfileException -> when (it) {
                        is ProfileRepository.ProfileException.EmptyUserId ->
                            onResult(Result.Error(EmptyUserId))

                        else -> onResult(Result.Exception(it))
                    }

                    is AlreadyUserException -> onResult(Result.Error(AlreadyUser))
                    else -> onResult(Result.Exception(it))
                }
            }
            .collect { onResult(Result.Success(it)) }
    }

    private suspend fun createProfile(entity: ProfileEntity) = with(repository) {
        runCatching { getProfile(entity.uid) }
            .getOrNull()
            ?.let { throw AlreadyUserException("already user id : ${it.uid}") }

        setProfile(entity)
        getProfile(entity.uid)
    }

    override suspend fun invoke(param: ProfileEntity, scope: CoroutineScope): ProfileEntity =
        scope.async { withContext(Dispatchers.IO) { createProfile(param) } }.await()

}