package kr.co.hs.sudoku.model.user

data class LocaleModel(val lang: String, val region: String) {
    constructor() : this("", "")

    override fun toString() = "${lang}_$region"
}