package kr.co.hs.sudoku.repository.challenge

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.repository.TestableRepository
import java.util.Date

class ChallengeReaderRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl(),
    private val recordRemoteSource: RecordRemoteSource = ChallengeRecordRemoteSourceImpl()
) : ChallengeReaderRepository, TestableRepository {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    override suspend fun getChallenge(challengeId: String) =
        remoteSource.getChallenge(challengeId).toDomain()?.apply {
            with(recordRemoteSource) {
                runCatching { getReservedMyRecord(challengeId, currentUserUid) }
                    .getOrNull()
                    ?.run {
                        startPlayAt = this.startAt?.toDate()
                        isPlaying = startPlayAt != null
                        relatedUid = uid
                    }
                runCatching { getRecord(challengeId, currentUserUid) }
                    .getOrNull()
                    ?.run { isComplete = this.clearTime >= 0 }
            }
        } ?: throw Exception("invalid challenge entity")

    override suspend fun getLatestChallenge() =
        remoteSource.getLatestChallenge().toDomain()?.apply {

            with(recordRemoteSource) {
                runCatching { getReservedMyRecord(challengeId, currentUserUid) }
                    .getOrNull()
                    ?.run {
                        startPlayAt = this.startAt?.toDate()
                        isPlaying = startPlayAt != null
                        relatedUid = uid
                    }
                runCatching { getRecord(challengeId, currentUserUid) }
                    .getOrNull()
                    ?.run { isComplete = this.clearTime >= 0 }
            }

        } ?: throw Exception("invalid challenge entity")

    override suspend fun getChallengeIds() = remoteSource.getChallengeIds()

    override suspend fun getChallenge(createdAt: Date): ChallengeEntity {
        return remoteSource.getChallenge(createdAt).toDomain() ?: throw Exception("not found")
    }

    override suspend fun getChallenges(startAt: Date) =
        remoteSource.getChallenges(startAt).mapNotNull { it.toDomain() }

    override suspend fun getChallenges(count: Long) =
        remoteSource.getChallenges(count).mapNotNull { it.toDomain() }

    override fun setFireStoreRootVersion(versionName: String) {
        (remoteSource as FireStoreRemoteSource).rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)

        (recordRemoteSource as FireStoreRemoteSource).rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)
    }

    override fun clearCache() {}
}