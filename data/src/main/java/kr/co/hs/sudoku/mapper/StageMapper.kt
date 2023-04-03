package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.model.stage.impl.StageBuilderImpl
import kr.co.hs.sudoku.model.sudoku.StageModel

object StageMapper {
    fun StageModel.AutoGenStageModel.toDomain() = with(StageBuilderImpl()) {
        setBox(boxSize, boxCount)
        autoGenerate(matrix)
        build()
    }

    fun StageModel.CustomStageModel.toDomain() = with(StageBuilderImpl()) {
        setBox(boxSize, boxCount)
        setStage(matrix)
        build()
    }
}