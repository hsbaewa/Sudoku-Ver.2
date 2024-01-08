package kr.co.hs.sudoku.repository.settings

interface RegistrationRepository {
    suspend fun isFirstAppOpened(): Boolean
    suspend fun appOpened()
    suspend fun hasSeenSinglePlayGuide(): Boolean
    suspend fun seenSinglePlayGuide()
    suspend fun hasSeenMultiPlayGuide(): Boolean
    suspend fun seenMultiPlayGuide()
    suspend fun hasSeenChallengeGuide(): Boolean
    suspend fun seenChallengeGuide()

    suspend fun clear()
}