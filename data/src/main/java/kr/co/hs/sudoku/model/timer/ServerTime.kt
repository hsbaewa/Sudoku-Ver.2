package kr.co.hs.sudoku.model.timer

import com.google.firebase.Timestamp

data class ServerTime(
    val serverTime: Timestamp? = null
) {
    constructor() : this(null)
}
