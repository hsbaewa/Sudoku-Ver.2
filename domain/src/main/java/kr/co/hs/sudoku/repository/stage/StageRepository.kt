package kr.co.hs.sudoku.repository.stage

interface StageRepository {
    fun getBoxSize(): Int
    fun getBoxCount(): Int
    fun getAutoGenerateMaskList(): List<List<Int>>
}