package kr.co.hs.sudoku.datasource.challenge

import kr.co.hs.sudoku.model.challenge.ChallengeModel
import java.util.Date

interface ChallengeRemoteSource {
    suspend fun getLatestChallenge(): ChallengeModel
    suspend fun getChallenge(id: String): ChallengeModel
    suspend fun createChallenge(challengeModel: ChallengeModel): Boolean
    suspend fun createChallenge(challengeModel: ChallengeModel, createdAt: Date): Boolean
    suspend fun removeChallenge(id: String): Boolean
    suspend fun getChallengeIds(): List<String>
    suspend fun getChallenge(createdAt: Date): ChallengeModel
    suspend fun getChallenges(startAt: Date): List<ChallengeModel>
    suspend fun getChallenges(count: Long): List<ChallengeModel>
}