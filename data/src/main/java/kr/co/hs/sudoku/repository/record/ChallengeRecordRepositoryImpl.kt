package kr.co.hs.sudoku.repository.record

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.repository.user.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.TestableRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRecordRepository
import java.util.Locale
import java.util.TreeSet

class ChallengeRecordRepositoryImpl : ChallengeRecordRepository, TestableRepository {

    private val remoteSource = ChallengeRecordRemoteSourceImpl()
    private val cachedMap = HashMap<String, RankerEntity>()

    private var challengeId: String? = null

    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("사용자 인증 정보가 없습니다. 게임 진행을 위해서는 먼저 사용자 인증이 필요합니다.")

    override fun setChallengeId(id: String) {
        cachedMap.clear()
        challengeId = id
    }

    override suspend fun getRecords(limit: Int) =
        if (cachedMap.isNotEmpty()) {
            cachedMap.values.toList()
        } else {
            challengeId
                ?.run {
                    remoteSource.getRecords(this, limit)
                        .map { it.toDomain() }
                        .onEach { cachedMap[it.uid] = it }
                }
                ?: throw NullPointerException("challenge id is null")
        }

    override suspend fun putRecord(entity: RankerEntity) =
        challengeId
            ?.let {
                with(entity) {
                    remoteSource.setRecord(it, toData())
                        .takeIf { it }
                        ?.also { cachedMap[this.uid] = this }
                        ?.also {
                            val treeSet = TreeSet<RankerEntity>()
                            treeSet.addAll(cachedMap.values.toList())
                            treeSet.forEachIndexed { index, rankerEntity ->
                                rankerEntity.rank = index + 1L
                            }
                        }
                        ?: false
                }
            }
            ?: throw NullPointerException("challenge id is null")

    override suspend fun putRecord(clearRecord: Long): Boolean {
        val profileRepository = ProfileRepositoryImpl()
        val profile = profileRepository.getProfile(currentUserUid)
        return putRecord(RankerEntity(profile, clearRecord))
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

    override suspend fun getRecord(uid: String) =
        cachedMap.takeIf { it.containsKey(uid) }
            ?.run { this[uid] }
            ?: run {
                challengeId
                    ?.let {
                        remoteSource.getRecord(it, uid).toDomain().apply {
                            takeIf { clearTime > 0 }?.let { cachedMap[it.uid] = it }
                        }
                    }
                    ?: throw NullPointerException("challenge id is null")
            }

    override suspend fun deleteRecord(uid: String): Boolean {
        return challengeId
            ?.let { remoteSource.deleteRecord(it, uid) }
            ?.takeIf { it }
            ?.run {
                cachedMap
                    .takeIf { it.containsKey(uid) }
                    ?.run { remove(uid) }
                true
            }
            ?: false
    }

    override fun setFireStoreRootVersion(versionName: String) {
        remoteSource.rootDocument = FirebaseFirestore.getInstance()
            .collection("version")
            .document(versionName)
    }
}