package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

interface CreateChallengeUseCase {
    operator fun invoke(entity: ChallengeEntity): Flow<Boolean>
}