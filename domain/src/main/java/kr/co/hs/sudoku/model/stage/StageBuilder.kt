package kr.co.hs.sudoku.model.stage

interface StageBuilder {
    fun setBox(size: Int, count: Int): StageBuilder
    fun autoGenerate(generateFilter: List<List<Int>>?): StageBuilder
    fun setStage(stageTable: List<List<Int>>): StageBuilder
    fun build(): Stage
    fun isImmutableCell(row: Int, column: Int): Boolean
    fun getRowCount(): Int
    fun getColumnCount(): Int
}