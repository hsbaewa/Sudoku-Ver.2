package kr.co.hs.sudoku.model.user.impl

import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import java.util.Date

data class OnlineUserEntityImpl(
    override val uid: String,
    override var displayName: String,
    override var message: String?,
    override var iconUrl: String?,
    override val locale: LocaleEntity?,
    override val checkedAt: Date
) : ProfileEntity.OnlineUserEntity