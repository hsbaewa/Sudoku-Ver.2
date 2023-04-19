package kr.co.hs.sudoku.datasource.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeModel

interface ChallengeRemoteSource {
    suspend fun getLatestChallenge(): ChallengeModel
    suspend fun getChallenge(id: String): ChallengeModel
    suspend fun createChallenge(challengeModel: ChallengeModel): Boolean
    suspend fun removeChallenge(id: String): Boolean
    suspend fun getChallengeIds(): List<String>
}