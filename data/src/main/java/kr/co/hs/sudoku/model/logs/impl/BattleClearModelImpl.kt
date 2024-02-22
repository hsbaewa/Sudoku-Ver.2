package kr.co.hs.sudoku.model.logs.impl

import com.google.firebase.Timestamp
import kr.co.hs.sudoku.model.logs.LogModel
import java.util.Date

data class BattleClearModelImpl(
    override var id: String,
    override val createdAt: Timestamp,
    override var uid: String,
    override var battleWith: List<String>,
    override var record: Long,
    override var battleId: String
) : LogModel.BattleClear {
    constructor() : this("", Timestamp(Date(0)), "", emptyList(), 0, "")
}