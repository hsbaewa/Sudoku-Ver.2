package kr.co.hs.sudoku.repository.challenge

import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain

class ChallengeReaderRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl(),
    private val recordRemoteSource: RecordRemoteSource = ChallengeRecordRemoteSourceImpl()
) : ChallengeReaderRepository {
    override suspend fun getChallenge(challengeId: String) =
        remoteSource.getChallenge(challengeId).toDomain().apply {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                with(recordRemoteSource) {
                    runCatching { getReservedMyRecord(challengeId, uid) }
                        .getOrNull()
                        ?.run {
                            startPlayAt = this.startAt?.toDate()
                            isPlaying = startPlayAt != null
                        }
                    runCatching { getRecord(challengeId, uid) }
                        .getOrNull()
                        ?.run { isComplete = this.clearTime >= 0 }
                }
            }
        }

    override suspend fun getLatestChallenge() =
        remoteSource.getLatestChallenge().toDomain().apply {

            challengeId.takeIf { it != null }?.let { challengeId ->
                FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                    with(recordRemoteSource) {
                        runCatching { getReservedMyRecord(challengeId, uid) }
                            .getOrNull()
                            ?.run {
                                startPlayAt = this.startAt?.toDate()
                                isPlaying = startPlayAt != null
                            }
                        runCatching { getRecord(challengeId, uid) }
                            .getOrNull()
                            ?.run { isComplete = this.clearTime >= 0 }
                    }
                }
            }

        }

    override suspend fun getChallengeIds() = remoteSource.getChallengeIds()
}