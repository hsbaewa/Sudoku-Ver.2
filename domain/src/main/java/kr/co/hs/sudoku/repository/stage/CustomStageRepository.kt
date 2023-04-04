package kr.co.hs.sudoku.repository.stage

import kr.co.hs.sudoku.model.stage.StageBuilder

interface CustomStageRepository {
    fun getStage(): StageBuilder
}