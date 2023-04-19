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
    constructor() : this("", "", null, null, null, 0L)
    constructor(profile: ProfileModel, clearTime: Long) : this(
        profile.uid,
        profile.name,
        profile.message,
        profile.iconUrl,
        profile.locale,
        clearTime
    )
}