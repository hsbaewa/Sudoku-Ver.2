package kr.co.hs.sudoku.model.sudoku

sealed interface StageModel : CellMatrixModel {
    val boxSize: Int
    val boxCount: Int
    val width: Int
    val height: Int

    interface AutoGenStageModel : StageModel
    interface CustomStageModel : StageModel
}