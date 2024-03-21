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
class GetProfileUseCase
@Inject constructor(
    private val repository: ProfileRepository
) : UseCase<String, ProfileEntity, GetProfileUseCase.Error>() {
    override fun invoke(
        param: String,
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ): Job = scope.launch {
        flow { emit(withContext(Dispatchers.IO) { getProfile(param) }) }
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

    private suspend fun getProfile(uid: String) = with(repository) {
        runCatching {
            getProfile(uid)
        }.getOrElse {
            when (it) {
                is NullPointerException ->
                    throw ProfileRepository.ProfileException.ProfileNotFound("profile not found")

                else -> throw it
            }
        }
    }

    override suspend fun invoke(param: String, scope: CoroutineScope): ProfileEntity =
        scope.async { withContext(Dispatchers.IO) { getProfile(param) } }.await()

    sealed interface Error
    object EmptyUserId : Error
    object ProfileNotFound : Error
}