package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.record.ClearTimeRecordModel
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl

object RecordMapper {
    fun ClearTimeRecordModel.toDomain() = RankerEntity(
        uid = uid,
        displayName = name,
        message = message,
        iconUrl = iconUrl,
        locale = LocaleEntityImpl(locale?.lang ?: "", locale?.region ?: ""),
        clearTime = clearTime
    )
}