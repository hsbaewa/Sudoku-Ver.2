package kr.co.hs.sudoku.model.record

import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileModel

data class ClearTimeRecordModel(
    override val uid: String,
    override val name: String,
    override val message: String?,
    override val iconUrl: String?,
    override val locale: LocaleModel?,
    val clearTime: Long
) : ProfileModel {
    constructor() : this("", "", null, null, null, -1L)

    var rank = -1L
}