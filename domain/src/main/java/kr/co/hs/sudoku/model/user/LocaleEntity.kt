package kr.co.hs.sudoku.model.user

import java.util.Locale

interface LocaleEntity {
    val lang: String
    val region: String
    fun toLocale(): Locale
    fun getLocaleFlag(): String
}