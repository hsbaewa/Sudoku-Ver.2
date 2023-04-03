package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.stage.Stage

interface CustomStageRepository {
    fun getStage(): Stage
}