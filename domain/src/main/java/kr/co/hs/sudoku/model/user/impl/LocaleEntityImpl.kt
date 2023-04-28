package kr.co.hs.sudoku.model.user.impl

import kr.co.hs.sudoku.model.user.LocaleEntity
import java.util.*

data class LocaleEntityImpl(
    override val lang: String,
    override val region: String
) : LocaleEntity {
    override fun toLocale() = Locale.Builder()
        .setLanguage(lang)
        .setRegion(region)
        .build()

    override fun getLocaleFlag(): String {
        val firstLetter = Character.codePointAt(region, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(region, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}