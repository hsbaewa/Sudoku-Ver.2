package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.stage.StageMask

interface StageRepository {
    fun getBoxSize(): Int
    fun getBoxCount(): Int
    fun getStageMask(): StageMask
}