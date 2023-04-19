package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeEntity

interface ChallengeWriterRepository {
    suspend fun createChallenge(entity: ChallengeEntity): Boolean
    suspend fun removeChallenge(entity: ChallengeEntity): Boolean
}