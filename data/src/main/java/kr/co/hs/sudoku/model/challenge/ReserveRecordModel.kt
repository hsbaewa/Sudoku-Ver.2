package kr.co.hs.sudoku.model.challenge

import com.google.firebase.Timestamp
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileModel

data class ReserveRecordModel(
    override val uid: String,
    override val name: String,
    override val message: String?,
    override val iconUrl: String?,
    override val locale: LocaleModel?
) : ProfileModel {
    var startAt: Timestamp? = null

    constructor() : this("", "", null, null, null)
}