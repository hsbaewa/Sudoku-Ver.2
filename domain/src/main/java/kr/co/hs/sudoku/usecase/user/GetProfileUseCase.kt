package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.user.ProfileRepository

class GetProfileUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(uid: String) = flow {
        val result = repository.getProfile(uid)
        emit(result)
    }
}