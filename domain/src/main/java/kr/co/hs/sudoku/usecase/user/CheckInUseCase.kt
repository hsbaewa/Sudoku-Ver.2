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
class CheckInUseCase
@Inject constructor(
    @ProfileRepositoryQualifier private val repository: ProfileRepository
) : UseCase<ProfileEntity, Unit, CheckInUseCase.Error>() {
    sealed interface Error
    object EmptyUserId : Error
    object UnKnownUser : Error


    override fun invoke(
        param: ProfileEntity,
        scope: CoroutineScope,
        onResult: (Result<Unit, Error>) -> Unit
    ): Job = scope.launch {
        flow<Unit> { withContext(Dispatchers.IO) { checkIn(param) } }
            .catch {
                when (it) {
                    is ProfileRepository.ProfileException -> when (it) {
                        is ProfileRepository.ProfileException.EmptyUserId ->
                            onResult(Result.Error(EmptyUserId))

                        is ProfileRepository.ProfileException.ProfileNotFound ->
                            onResult(Result.Error(UnKnownUser))
                    }

                    else -> onResult(Result.Exception(it))
                }
            }
            .collect { onResult(Result.Success(it)) }
    }

    private suspend fun checkIn(profile: ProfileEntity) = with(repository) {
        if (profile.uid.isEmpty())
            throw ProfileRepository.ProfileException.EmptyUserId("user id is empty")

        val checkProfile = runCatching {
            getProfile(profile.uid)
        }.getOrElse {
            when (it) {
                is NullPointerException ->
                    throw ProfileRepository.ProfileException.ProfileNotFound(it.message)

                else -> throw it
            }
        }

        checkIn(checkProfile.uid)
    }

    override suspend fun invoke(param: ProfileEntity, scope: CoroutineScope): Unit =
        scope.async { withContext(Dispatchers.IO) { checkIn(param) } }.await()
}