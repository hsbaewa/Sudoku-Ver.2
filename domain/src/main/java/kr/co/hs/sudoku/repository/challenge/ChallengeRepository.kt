package kr.co.hs.sudoku.repository.challenge

interface ChallengeRepository :
    ChallengeWriterRepository,
    ChallengeReaderRepository,
    ChallengeRecordRepository {
    suspend fun setPlaying(challengeId: String): Boolean
}