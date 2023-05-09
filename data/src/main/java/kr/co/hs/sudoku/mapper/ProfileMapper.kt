package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.ProfileModelImpl
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl

object ProfileMapper {
    fun LocaleModel?.toDomain() = this?.run { LocaleEntityImpl(lang, region) }

    fun ProfileModelImpl.toDomain() = ProfileEntityImpl(
        uid = uid,
        displayName = name,
        message = message ?: "",
        iconUrl = iconUrl ?: "",
        locale = locale.toDomain()
    )

    fun ProfileEntity.toData() = ProfileModelImpl(
        uid = uid,
        name = displayName,
        message = message,
        iconUrl = iconUrl,
        locale = locale?.run { LocaleModel(lang, region) }
    )
}