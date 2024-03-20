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
class CheckOutUseCase
@Inject constructor(
    @ProfileRepositoryQualifier private val repository: ProfileRepository
) : UseCase<String, ProfileEntity, CheckOutUseCase.Error>() {

    sealed interface Error
    object EmptyUserId : Error
    object UnKnownUser : Error

    override fun invoke(
        param: String,
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ): Job = scope.launch {
        flow<ProfileEntity> { withContext(Dispatchers.IO) { checkOut(param) } }
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

    operator fun invoke(
        profileEntity: ProfileEntity,
        scope: CoroutineScope,
        onResult: (Result<ProfileEntity, Error>) -> Unit
    ): Job = invoke(profileEntity.uid, scope, onResult)

    private suspend fun checkOut(uid: String) = with(repository) {
        if (uid.isEmpty())
            throw ProfileRepository.ProfileException.EmptyUserId("user id is empty")

        runCatching {
            checkOut(uid)
        }.getOrElse {
            when (it) {
                is NullPointerException ->
                    throw ProfileRepository.ProfileException.ProfileNotFound(it.message)

                else -> throw it
            }
        }
    }

    override suspend fun invoke(param: String, scope: CoroutineScope): ProfileEntity =
        scope.async { withContext(Dispatchers.IO) { checkOut(param) } }.await()

    suspend operator fun invoke(
        profileEntity: ProfileEntity,
        scope: CoroutineScope
    ): ProfileEntity = invoke(profileEntity.uid, scope)
}