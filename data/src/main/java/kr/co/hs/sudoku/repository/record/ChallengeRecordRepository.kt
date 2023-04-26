package kr.co.hs.sudoku.repository.record

import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import java.util.Locale
import java.util.TreeSet

class ChallengeRecordRepository(val challengeId: String) : RecordRepository {

    private val remoteSource = ChallengeRecordRemoteSourceImpl()
    private val cachedMap = HashMap<String, RankerEntity>()

    override suspend fun getRecords(limit: Int) =
        if (cachedMap.isNotEmpty()) {
            cachedMap.values.toList()
        } else {
            remoteSource.getRecords(challengeId, limit)
                .map { it.toDomain() }
                .onEach { cachedMap[it.uid] = it }
        }

    override suspend fun putRecord(entity: RankerEntity) =
        with(entity) {
            remoteSource.setRecord(challengeId, toData())
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
            ?: remoteSource.getRecord(challengeId, uid).toDomain().also {
                cachedMap[it.uid] = it
            }

}