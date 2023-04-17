package kr.co.hs.sudoku.model.user.impl

import kr.co.hs.sudoku.model.user.LocaleEntity
import java.util.*

data class LocaleEntityImpl(
    override val lang: String,
    override val region: String
) : LocaleEntity {
    fun getLocale() = Locale.Builder()
        .setLanguage(lang)
        .setRegion(region)
        .build()
}