package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeWriterRepository

class CreateChallengeUseCaseImpl(
    private val repository: ChallengeWriterRepository
) : CreateChallengeUseCase {
    override fun invoke(entity: ChallengeEntity) = flow { emit(repository.createChallenge(entity)) }
}