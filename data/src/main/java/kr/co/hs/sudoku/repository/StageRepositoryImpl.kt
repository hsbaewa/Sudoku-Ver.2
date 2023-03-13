package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.model.stage.StageMask
import kr.co.hs.sudoku.model.stage.impl.StageMaskImpl
import kr.co.hs.sudoku.repository.stage.StageRepository

class StageRepositoryImpl : StageRepository {

    private val stageMaskList = listOf(
        StageMaskImpl(
            listOf(
                listOf(1, 1, 0, 0, 1, 0, 0, 0, 0),
                listOf(1, 0, 0, 1, 1, 1, 0, 0, 0),
                listOf(1, 1, 0, 0, 0, 0, 0, 1, 0),
                listOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
                listOf(1, 0, 0, 1, 0, 1, 0, 0, 1),
                listOf(1, 0, 0, 0, 1, 0, 0, 0, 1),
                listOf(0, 1, 0, 0, 0, 0, 1, 1, 0),
                listOf(0, 0, 0, 1, 1, 1, 0, 0, 1),
                listOf(0, 0, 0, 0, 1, 0, 0, 1, 1)
            )
        )
    )

    override fun getBoxSize(): Int {
        return 3
    }

    override fun getBoxCount(): Int {
        return 3
    }

    override fun getStageMask(): StageMask {
        return stageMaskList[0]
    }
}