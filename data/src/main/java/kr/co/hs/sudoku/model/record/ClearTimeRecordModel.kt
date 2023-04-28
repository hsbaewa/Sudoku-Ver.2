package kr.co.hs.sudoku.model.record

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileModel

@IgnoreExtraProperties
data class ClearTimeRecordModel(
    override val uid: String,
    override val name: String,
    override val message: String?,
    override val iconUrl: String?,
    override val locale: LocaleModel?,
    val clearTime: Long
) : ProfileModel {
    constructor() : this("", "", null, null, null, -1L)

    @get:Exclude
    var rank: Long? = null
}