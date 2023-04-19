package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.challenge.ChallengeReaderRepository

class GetChallengeListUseCaseImpl(
    private val repository: ChallengeReaderRepository
) : GetChallengeListUseCase {
    override fun invoke() = flow { emit(repository.getChallengeIds()) }
}