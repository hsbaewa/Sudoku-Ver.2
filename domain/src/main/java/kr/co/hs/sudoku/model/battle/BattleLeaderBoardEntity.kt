package kr.co.hs.sudoku.model.battle

data class BattleLeaderBoardEntity(
    val uid: String,
    val playCount: Long,
    val winCount: Long,
    var ranking: Long,
    var displayName: String? = null
)