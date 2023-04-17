package kr.co.hs.sudoku.usecase.user

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository

class SetProfileUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(profileEntity: ProfileEntity) = flow {
        emit(repository.setProfile(profileEntity))
    }
}