package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.datasource.rank.impl.ChallengeRankingRemoteSourceImpl
import kr.co.hs.sudoku.mapper.RecordMapper.toDomain
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.repository.rank.RankingRepository
import java.util.Locale

class ChallengeRankingRepository(challengeId: String) : RankingRepository {

    private val remoteSource = ChallengeRankingRemoteSourceImpl(challengeId)

    override suspend fun getRanking() = remoteSource.getRecords().map { it.toDomain() }
    override suspend fun putRecord(entity: RankerEntity) = remoteSource.addRecord(entity.toData())
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

    override suspend fun getRecord(uid: String) = remoteSource.getRecord(uid).toDomain()
}