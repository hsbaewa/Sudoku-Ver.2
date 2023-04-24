package kr.co.hs.sudoku.usecase.challenge

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

interface GetChallengeUseCase {
    operator fun invoke(): Flow<ChallengeEntity>
    operator fun invoke(id: String): Flow<ChallengeEntity>
}