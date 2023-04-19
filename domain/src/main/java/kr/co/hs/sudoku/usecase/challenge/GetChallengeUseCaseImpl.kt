package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.challenge.ChallengeReaderRepository

class GetChallengeUseCaseImpl(
    private val repository: ChallengeReaderRepository
) : GetChallengeUseCase {
    override fun invoke() = flow { emit(repository.getLatestChallenge()) }
}