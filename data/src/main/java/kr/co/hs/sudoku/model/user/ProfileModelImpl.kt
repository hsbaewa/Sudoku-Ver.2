package kr.co.hs.sudoku.model.user

data class ProfileModelImpl(
    override val uid: String,
    override val name: String,
    override val message: String?,
    override val iconUrl: String?,
    override val locale: LocaleModel?
) : ProfileModel {
    constructor() : this("", "", "", "", null)
}