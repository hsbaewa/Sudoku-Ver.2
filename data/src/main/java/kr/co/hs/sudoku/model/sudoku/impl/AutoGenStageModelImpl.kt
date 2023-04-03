package kr.co.hs.sudoku.model.sudoku.impl

import kr.co.hs.sudoku.model.sudoku.StageModel

data class AutoGenStageModelImpl(
    override val boxSize: Int,
    override val boxCount: Int,
    override val width: Int,
    override val height: Int,
    override val matrix: List<List<Int>>
) : StageModel.AutoGenStageModel