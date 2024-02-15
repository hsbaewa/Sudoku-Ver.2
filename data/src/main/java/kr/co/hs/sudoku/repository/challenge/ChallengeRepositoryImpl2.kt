package kr.co.hs.sudoku.repository.challenge

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kr.co.hs.sudoku.datasource.FireStoreRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.ChallengeMapper.toDomain
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.challenge.ChallengeModel
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.repository.TestableRepository
import kr.co.hs.sudoku.repository.user.ProfileRepositoryImpl
import java.util.Date
import java.util.Locale

class ChallengeRepositoryImpl2(
    private var challengeRemoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl(),
    private var recordRemoteSource: RecordRemoteSource = ChallengeRecordRemoteSourceImpl()
) : ChallengeRepository2, TestableRepository {

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    override suspend fun getChallenges(createdAt: Date, count: Long): List<ChallengeEntity> {
        return challengeRemoteSource.getChallenges(createdAt, count)
            .mapNotNull { it.toDomain() }
            .onEach { recordRemoteSource.getChallengeMetadata(it, currentUserUid) }
    }

    override suspend fun createChallenge(entity: ChallengeEntity) =
        entity.createdAt.takeIf { it != null }
            ?.run { challengeRemoteSource.createChallenge(entity.toData(), this) }
            ?: run { challengeRemoteSource.createChallenge(entity.toData()) }

    private fun ChallengeEntity.toData() = ChallengeModel(
        boxSize = matrix.boxSize,
        boxCount = matrix.boxCount,
        rowCount = matrix.rowCount,
        columnCount = matrix.columnCount,
        matrix = matrix.flatten()
    ).apply { id = challengeId }

    override suspend fun removeChallenge(challengeId: String): Boolean {
        return challengeRemoteSource.removeChallenge(challengeId)
    }

    override fun clearCache() {}

    override suspend fun getRecords(challengeId: String): List<RankerEntity> {
        return recordRemoteSource.getRecords(challengeId, 10).map { it.toDomain() }
    }

    override suspend fun getRecord(challengeId: String, uid: String): RankerEntity {
        return recordRemoteSource.getRecord(challengeId, uid).toDomain()
    }

    override suspend fun putRecord(challengeId: String, clearRecord: Long): Boolean {
        val profileRepository = ProfileRepositoryImpl()
        val profile = profileRepository.getProfile(currentUserUid)
        return recordRemoteSource.setRecord(
            challengeId,
            RankerEntity(profile, clearRecord).toData()
        )
    }

    private fun RankerEntity.toData() = ClearTimeRecordModel(
        uid = uid,
        name = displayName,
        message = message,
        iconUrl = iconUrl,
        locale = locale?.toData(),
        clearTime = clearTime
    )

    private fun LocaleEntity?.toData() = LocaleModel(
        this?.lang ?: Locale.getDefault().language,
        this?.region ?: Locale.getDefault().country
    )

    override suspend fun deleteRecord(challengeId: String, uid: String): Boolean {
        return recordRemoteSource.deleteRecord(challengeId, uid)
    }

    override fun setFireStoreRootVersion(versionName: String) {
        val root = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)

        (challengeRemoteSource as FireStoreRemoteSource).rootDocument = root
        (recordRemoteSource as FireStoreRemoteSource).rootDocument = root
    }
}