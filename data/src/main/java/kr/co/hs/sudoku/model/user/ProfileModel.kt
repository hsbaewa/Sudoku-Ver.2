package kr.co.hs.sudoku.model.user

interface ProfileModel {
    val uid: String
    val name: String
    val message: String?
    val iconUrl: String?
    val locale: LocaleModel?
}