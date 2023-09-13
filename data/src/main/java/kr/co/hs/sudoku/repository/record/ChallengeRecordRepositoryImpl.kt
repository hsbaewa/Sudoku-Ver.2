package kr.co.hs.sudoku.repository.record

import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.repository.challenge.ChallengeRecordRepository
import java.util.Locale
import java.util.TreeSet

class ChallengeRecordRepositoryImpl : ChallengeRecordRepository {

    private val remoteSource = ChallengeRecordRemoteSourceImpl()
    private val cachedMap = HashMap<String, RankerEntity>()

    private var challengeId: String? = null
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


}