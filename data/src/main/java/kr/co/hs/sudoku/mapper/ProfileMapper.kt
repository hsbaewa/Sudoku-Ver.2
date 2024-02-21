package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileModel
import kr.co.hs.sudoku.model.user.ProfileModelImpl
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.model.user.impl.OnlineUserEntityImpl
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl

object ProfileMapper {
    fun LocaleModel?.toDomain() = this?.run { LocaleEntityImpl(lang, region) }

    fun <T : ProfileModel> T.toDomain() = when (this) {
        is ProfileModelImpl -> status?.takeIf { it == "in" }
            ?.run {
                checkedAt?.toDate()
                    ?.let { checkedDate ->
                        OnlineUserEntityImpl(
                            uid = uid,
                            displayName = name,
                            message = message ?: "",
                            iconUrl = iconUrl ?: "",
                            locale = locale.toDomain(),
                            checkedAt = checkedDate
                        )
                    }
                    ?: ProfileEntityImpl(
                        uid = uid,
                        displayName = name,
                        message = message ?: "",
                        iconUrl = iconUrl ?: "",
                        locale = locale.toDomain(),
                        lastCheckedAt = null
                    )
            }
            ?: run {
                ProfileEntityImpl(
                    uid = uid,
                    displayName = name,
                    message = message ?: "",
                    iconUrl = iconUrl ?: "",
                    locale = locale.toDomain(),
                    lastCheckedAt = checkedAt?.toDate()
                )
            }

        else -> ProfileEntityImpl(
            uid = uid,
            displayName = name,
            message = message ?: "",
            iconUrl = iconUrl ?: "",
            locale = locale.toDomain(),
            lastCheckedAt = null
        )
    }
}