package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel

object RecordMapper {
    fun ClearTimeRecordModel.toDomain() = RankerEntity(
        uid = uid,
        displayName = name,
        message = message,
        iconUrl = iconUrl,
        locale = locale.toDomain(),
        rank = rank ?: -1L,
        clearTime = clearTime
    )
}