package kr.co.hs.sudoku.repository.challenge

interface ChallengeRepository : ChallengeWriterRepository, ChallengeReaderRepository {
    suspend fun setPlaying(challengeId: String): Boolean
}