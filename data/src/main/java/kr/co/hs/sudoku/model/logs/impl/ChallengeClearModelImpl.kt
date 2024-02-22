package kr.co.hs.sudoku.model.logs.impl

import com.google.firebase.Timestamp
import kr.co.hs.sudoku.model.logs.LogModel
import java.util.Date

data class ChallengeClearModelImpl(
    override var id: String,
    override val createdAt: Timestamp,
    override var uid: String,
    override var challengeId: String,
    override var record: Long
) : LogModel.ChallengeClear {
    constructor() : this("", Timestamp(Date(0)), "", "", 0)
}