package kr.co.hs.sudoku.repository.challenge

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.record.ReserveRecordModel
import kr.co.hs.sudoku.repository.TestableRepository
import kr.co.hs.sudoku.repository.record.ChallengeRecordRepositoryImpl

class ChallengeRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl(),
    private val recordRemoteSource: RecordRemoteSource = ChallengeRecordRemoteSourceImpl(),
    private val reader: ChallengeReaderRepository = ChallengeReaderRepositoryImpl(
        remoteSource,
        recordRemoteSource
    ),
    private val writer: ChallengeWriterRepository = ChallengeWriterRepositoryImpl(remoteSource),
    private val challengeRecordRepository: ChallengeRecordRepositoryImpl = ChallengeRecordRepositoryImpl()
) : ChallengeRepository,
    ChallengeReaderRepository by reader,
    ChallengeWriterRepository by writer,
    ChallengeRecordRepository by challengeRecordRepository,
    TestableRepository {

    private val cachedMap = HashMap<String, ChallengeEntity>()

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    override suspend fun setPlaying(challengeId: String): Boolean {
        val reserveModel = ReserveRecordModel(currentUserUid, "", null, null, null)
        recordRemoteSource.setRecord(id = challengeId, reserveModel)
            .takeIf { it }
            ?.run { cachedMap.remove(challengeId) }
        return true
    }

    override suspend fun getChallenge(challengeId: String): ChallengeEntity {
        return cachedMap.takeIf { it.containsKey(challengeId) }
            ?.run { cachedMap[challengeId] }
            ?: reader.getChallenge(challengeId)
                .also {
                    cachedMap[it.challengeId] = it
                    setChallengeId(it.challengeId)
                }
    }

    override suspend fun getLatestChallenge(): ChallengeEntity {
        return reader.getLatestChallenge()
            .also { setChallengeId(it.challengeId) }
    }

    override suspend fun deleteRecord(uid: String): Boolean {
        return challengeRecordRepository.deleteRecord(uid).apply {
            if (this) {
                with(cachedMap) {
                    filterValues { it.relatedUid == uid }
                        .forEach { remove(it.key) }
                }
            }
        }
    }

    override fun setFireStoreRootVersion(versionName: String) {
        (remoteSource as FireStoreRemoteSource).rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)

        (recordRemoteSource as FireStoreRemoteSource).rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)

        challengeRecordRepository.setFireStoreRootVersion(versionName)
    }
}