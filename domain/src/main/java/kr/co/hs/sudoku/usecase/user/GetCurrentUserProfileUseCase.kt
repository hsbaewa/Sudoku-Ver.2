package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCurrentUserProfileUseCase
@Inject constructor(
    private val repository: ProfileRepository
) : NoErrorUseCase<Unit, ProfileEntity>() {

    override fun invoke(param: Unit): Flow<Result<ProfileEntity>> = flow {
        emit(repository.getProfile())
    }.map {
        Result.Success(it)
    }

    operator fun invoke() = invoke(Unit)
}