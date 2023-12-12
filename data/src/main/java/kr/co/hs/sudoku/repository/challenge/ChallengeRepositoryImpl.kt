package kr.co.hs.sudoku.repository.challenge

import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.record.ReserveRecordModel
import kr.co.hs.sudoku.repository.record.ChallengeRecordRepositoryImpl

class ChallengeRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl(),
    private val recordRemoteSource: RecordRemoteSource = ChallengeRecordRemoteSourceImpl(),
    private val reader: ChallengeReaderRepository = ChallengeReaderRepositoryImpl(remoteSource),
    private val writer: ChallengeWriterRepository = ChallengeWriterRepositoryImpl(remoteSource),
    private val challengeRecordRepository: ChallengeRecordRepositoryImpl = ChallengeRecordRepositoryImpl()
) : ChallengeRepository,
    ChallengeReaderRepository by reader,
    ChallengeWriterRepository by writer,
    ChallengeRecordRepository by challengeRecordRepository {

    private val cachedMap = HashMap<String, ChallengeEntity>()

    override suspend fun setPlaying(challengeId: String): Boolean {
        FirebaseAuth.getInstance().currentUser?.run {
            val reserveModel = ReserveRecordModel(uid, "", null, null, null)
            recordRemoteSource.setRecord(id = challengeId, reserveModel)
                .takeIf { it }
                ?.run { cachedMap.remove(challengeId) }
        }
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
}