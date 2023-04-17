package kr.co.hs.sudoku.model.user

interface ProfileEntity {
    val uid: String
    var displayName: String
    var message: String
    var iconUrl: String
    val locale: LocaleEntity?
}