package kr.co.hs.sudoku.model.user.impl

import kr.co.hs.sudoku.model.user.ProfileEntity

data class ProfileEntityImpl(
    override val uid: String,
    override var displayName: String,
    override var message: String,
    override var iconUrl: String,
    override var locale: LocaleEntityImpl?
) : ProfileEntity