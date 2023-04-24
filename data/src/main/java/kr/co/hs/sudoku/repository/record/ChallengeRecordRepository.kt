package kr.co.hs.sudoku.repository.record

import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import java.util.Locale

class ChallengeRecordRepository(val challengeId: String) : RecordRepository {

    private val remoteSource = ChallengeRecordRemoteSourceImpl()

    override suspend fun getRecords(limit: Int) =
        remoteSource.getRecords(challengeId, limit).map { it.toDomain() }

    override suspend fun putRecord(entity: RankerEntity) =
        remoteSource.addRecord(challengeId, entity.toData())

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
        remoteSource.getRecord(challengeId, uid).toDomain()
}