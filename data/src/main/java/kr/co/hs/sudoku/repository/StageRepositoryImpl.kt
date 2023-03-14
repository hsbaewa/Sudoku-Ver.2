package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.repository.stage.StageRepository

class StageRepositoryImpl : StageRepository {

    override fun getBoxSize(): Int {
        return 3
    }

    override fun getBoxCount(): Int {
        return 3
    }

    override fun getAutoGenerateMaskList() =
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
}