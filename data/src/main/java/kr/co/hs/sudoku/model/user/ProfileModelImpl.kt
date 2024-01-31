package kr.co.hs.sudoku.model.user

import com.google.firebase.Timestamp

data class ProfileModelImpl(
    override val uid: String,
    override val name: String,
    override val message: String?,
    override val iconUrl: String?,
    override val locale: LocaleModel?,
    val checkedAt: Timestamp?,
    val status: String?
) : ProfileModel {
    constructor() : this("", "", "", "", null, null, null)
}