package kr.co.hs.sudoku.model.user

import java.util.Date

sealed interface ProfileEntity {
    val uid: String
    var displayName: String
    var message: String?
    var iconUrl: String?
    val locale: LocaleEntity?

    interface UserEntity : ProfileEntity
    interface OnlineUserEntity : ProfileEntity {
        val checkedAt: Date
    }
}