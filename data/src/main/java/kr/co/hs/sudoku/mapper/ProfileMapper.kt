package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileModel
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl

object ProfileMapper {
    fun LocaleModel.toDomain() = LocaleEntityImpl(
        lang = lang,
        region = region
    )

    fun ProfileModel.toDomain() = ProfileEntityImpl(
        uid = uid,
        displayName = name,
        message = message,
        iconUrl = iconUrl,
        locale = locale?.toDomain()
    )
}