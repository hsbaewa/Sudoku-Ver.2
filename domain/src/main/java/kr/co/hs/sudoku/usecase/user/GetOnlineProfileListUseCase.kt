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
class GetOnlineProfileListUseCase
@Inject constructor(
    @ProfileRepositoryQualifier private val repository: ProfileRepository
) : UseCase<Unit, List<ProfileEntity>, Nothing>() {

    override fun invoke(
        param: Unit,
        scope: CoroutineScope,
        onResult: (Result<List<ProfileEntity>, Nothing>) -> Unit
    ): Job = scope.launch {
        flow { emit(withContext(Dispatchers.IO) { repository.getOnlineUserList() }) }
            .catch { onResult(Result.Exception(it)) }
            .collect { onResult(Result.Success(it)) }
    }

    operator fun invoke(
        scope: CoroutineScope,
        onResult: (Result<List<ProfileEntity>, Nothing>) -> Unit
    ) = invoke(Unit, scope, onResult)


    override suspend fun invoke(param: Unit, scope: CoroutineScope): List<ProfileEntity> =
        scope.async { withContext(Dispatchers.IO) { repository.getOnlineUserList() } }.await()

    suspend operator fun invoke(scope: CoroutineScope) = invoke(Unit, scope)
}