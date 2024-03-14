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
class UpdateProfileUseCase
@Inject constructor(
    private val repository: ProfileRepository
) : UseCase<ProfileEntity, ProfileEntity, UpdateProfileUseCase.Error>() {

    sealed interface Error
    object EmptyUserId : Error
    object ProfileNotFound : Error

    override fun invoke(
        param: ProfileEntity,
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ): Job = scope.launch {
        flow { emit(withContext(Dispatchers.IO) { updateProfile(param) }) }
            .catch {
                when (it) {
                    is ProfileRepository.ProfileException -> when (it) {
                        is ProfileRepository.ProfileException.EmptyUserId ->
                            onResult(Result.Error(EmptyUserId))

                        is ProfileRepository.ProfileException.ProfileNotFound ->
                            onResult(Result.Error(ProfileNotFound))
                    }

                    else -> onResult(Result.Exception(it))
                }
            }
            .collect { onResult(Result.Success(it)) }
    }

    private suspend fun updateProfile(entity: ProfileEntity) = with(repository) {
        val profile = runCatching { getProfile(entity.uid) }.getOrNull()
            ?.also {
                it.displayName = entity.displayName
                entity.message?.run { it.message = this }
                entity.iconUrl?.run { it.iconUrl = this }
            }
            ?: throw ProfileRepository.ProfileException.ProfileNotFound("profile not found : ${entity.uid}")

        setProfile(profile)
        getProfile(entity.uid)
    }

    override suspend fun invoke(param: ProfileEntity, scope: CoroutineScope) =
        scope.async { withContext(Dispatchers.IO) { updateProfile(param) } }.await()
}