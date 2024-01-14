package kr.co.hs.sudoku.model.battle

class BattleStatisticsModel {
    var uid: String = ""
    var winCount: Long = 0
    var playCount: Long = 0

    constructor()
    constructor(uid: String, winCount: Long, playCount: Long) {
        this.uid = uid
        this.winCount = winCount
        this.playCount = playCount
    }
}