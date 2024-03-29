package kr.co.hs.sudoku

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.model.matrix.IntermediateMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.MutableStage
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.*
import kr.co.hs.sudoku.usecase.AutoGenerateSudokuUseCase
import kr.co.hs.sudoku.usecase.PlaySudokuUseCaseImpl
import org.junit.Assert.*
import org.junit.Test

class StageEntityTest : IntCoordinateCellEntity.ValueChangedListener {

    private var cell: IntCoordinateCellEntity? = null

    @Test
    fun testMutableSudokuStageMod1() {
        val table = List(9) { row ->
            MutableList<IntCoordinateCellEntity>(9) { column ->
                IntCoordinateCellEntityImpl(row, column)
            }
        }
        val sudoku: MutableStage = MutableStageImpl(3, 3, table)
        sudoku.addValueChangedListener(this)

        assertEquals(81, sudoku.getEmptyCellCount())
        sudoku[1, 1] = 2
        cell.assertCell(1, 1, 2)
        sudoku[7, 7] = 5
        cell.assertCell(7, 7, 5)
        assertEquals(79, sudoku.getEmptyCellCount())

        assertEquals(2, table[1][1].getValue())
        assertEquals(table[1][1].getValue(), sudoku[1, 1])
        assertEquals(5, table[7][7].getValue())
        assertEquals(table[7][7].getValue(), sudoku[7, 7])

        sudoku.getCell(0, 2).setValue(3)
        cell.assertCell(0, 2, 3)

        val box = sudoku.getBoxBoundedIn(table[7][7])

        assertEquals(5, box.getValue(1, 1))
        assertEquals(2, sudoku.getBox(0, 0).getValue(1, 1))

        assertEquals(0, sudoku.getDuplicatedCellCount())
        sudoku.setValue(1, 2, 2)
        cell.assertCell(1, 2, 2)
        assertEquals(2, sudoku.getDuplicatedCellCount())
        sudoku.setValue(8, 8, 5)
        cell.assertCell(8, 8, 5)
        assertEquals(4, sudoku.getDuplicatedCellCount())
    }

    private fun IntCoordinateCellEntity?.assertCell(row: Int, column: Int, value: Int) {
        assertNotNull(this)
        this?.run {
            assertEquals(row, this.row)
            assertEquals(column, this.column)
            assertEquals(value, this.getValue())
        }
    }

    @Test
    fun testMutableSudokuStageMod2() {
        val sudoku: Stage = MutableStageImpl(2, 2)
        sudoku.addValueChangedListener(this)
        assertEquals(16, sudoku.getEmptyCellCount())
        assertEquals(0, sudoku.getDuplicatedCellCount())

        val box = sudoku.getBox(0, 0)
        assertEquals(setOf(1, 2, 3, 4), box.getAvailableValueInBox().toSet())

        sudoku[0, 0] = 1
        assertEquals(setOf(2, 3, 4), box.getAvailableValueInBox().toSet())
        sudoku[0, 1] = 1
        assertEquals(setOf(2, 3, 4), box.getAvailableValueInBox().toSet())
        assertEquals(2, sudoku.getDuplicatedCellCount())

        sudoku.clear(0, 1)
        assertEquals(0, sudoku.getDuplicatedCellCount())

        sudoku[0, 0] = 1
        sudoku[0, 1] = 2
        assertEquals(setOf(3, 4), box.getAvailableValueInBox().toSet())
        sudoku[1, 0] = 3
        assertEquals(setOf(4), box.getAvailableValueInBox().toSet())
        sudoku[1, 1] = 4
        assertEquals(0, box.getAvailableValueInBox().size)
        assertEquals(true, sudoku.getBox(0, 0).isSudokuClear())

        sudoku[0, 2] = 5
        assertEquals(true, sudoku.getCell(0, 2).isEmpty())

        sudoku[0, 2] = 1
        sudoku[0, 3] = 2
        sudoku[1, 2] = 3
        sudoku[1, 3] = 4
        assertEquals(true, sudoku.getBox(0, 1).isSudokuClear())

        assertEquals(8, sudoku.getDuplicatedCellCount())

        sudoku[0, 2] = 3
        sudoku[0, 3] = 4
        sudoku[1, 2] = 1
        sudoku[1, 3] = 2
        assertEquals(0, sudoku.getDuplicatedCellCount())

        sudoku[2, 0] = 2
        sudoku[2, 1] = 1
        sudoku[2, 2] = 4
        sudoku[2, 3] = 3

        sudoku[3, 0] = 4
        sudoku[3, 1] = 3
        sudoku[3, 2] = 2
        sudoku[3, 3] = 1

        println(sudoku)

        assertEquals(true, sudoku.isSudokuClear())
    }

    override fun onChanged(cell: IntCoordinateCellEntity) {
        this.cell = cell
    }

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
    @Test
    fun testAvailable() {
        val sudoku = MutableStageImpl(2, 2)
        var available = sudoku.getAvailable(0, 0)
        sudoku[0, 0] = 1
        available = sudoku.getAvailable(0, 1)
        sudoku[0, 1] = 2
        available = sudoku.getAvailable(0, 2)
        sudoku[0, 2] = 3
        available = sudoku.getAvailable(0, 3)
        sudoku[0, 3] = 4
        available = sudoku.getAvailable(1, 0)
        sudoku[1, 0] = 3
        available = sudoku.getAvailable(1, 1)
        sudoku[1, 1] = 4
        available = sudoku.getAvailable(1, 2)
        sudoku[1, 2] = 1
        available = sudoku.getAvailable(1, 3)
        sudoku[1, 3] = 2
        available = sudoku.getAvailable(2, 0)
        sudoku[2, 0] = 2
        available = sudoku.getAvailable(2, 1)
        sudoku[2, 1] = 1
        available = sudoku.getAvailable(2, 2)
        sudoku[2, 2] = 4
        available = sudoku.getAvailable(2, 3)
        sudoku[2, 3] = 3
        available = sudoku.getAvailable(3, 0)
        sudoku[3, 0] = 4
        available = sudoku.getAvailable(3, 1)
        sudoku[3, 1] = 3
        available = sudoku.getAvailable(3, 2)
        sudoku[3, 2] = 2
        available = sudoku.getAvailable(3, 3)
        sudoku[3, 3] = 1
        assertEquals(true, sudoku.isSudokuClear())
    }

    @Test
    fun testBuilder() {
        val stage = buildStage()
        assertThrows(IllegalArgumentException::class.java) {
            stage.set(0, 0, 1)
        }

        @Suppress("UNUSED_VARIABLE") val cell = stage.getCell(0, 2)
        stage[0, 5] = 1
    }

    private fun buildStage(): Stage {
        val matrix = IntermediateMatrix(
            boxSize = 3,
            boxCount = 3,
            matrix = listOf(listOf(1, 2, 3, 4)),
        )
        val buildUseCase = AutoGenerateSudokuUseCase(
            matrix = matrix,
            filterMask = listOf(
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
        return runBlocking {
            val stage = buildUseCase().first()
            println(stage)
            stage
        }
    }

    @Test
    fun testAutoFill() {
//        val matrix = IntermediateMatrix()
//        val buildUseCase = AutoGenerateSudokuUseCase(
//            matrix = matrix,
//            filterMask = listOf(
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
//                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
//            )
//        )

        val matrix = BeginnerMatrix()
        val buildUseCase = AutoGenerateSudokuUseCase(
            matrix = matrix,
            filterMask = listOf(
                listOf(1, 1, 1, 1),
                listOf(1, 1, 1, 1),
                listOf(1, 1, 1, 1),
                listOf(1, 1, 1, 1)
            )
        )
        val stage = runBlocking { buildUseCase().first() }
        assertEquals(true, stage.isSudokuClear())
    }

    @Test
    fun testCustomFill() {
        val matrix = IntermediateMatrix(
            matrix = listOf(
                listOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
                listOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
                listOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
                listOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
                listOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
                listOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
                listOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
                listOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
                listOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
            )
        )
        val buildUseCase = AutoGenerateSudokuUseCase(matrix)
        val stage = runBlocking { buildUseCase().first() }
        assertEquals(false, stage.isSudokuClear())
        assertEquals(0, stage.getDuplicatedCellCount())

    }

//    @Test
//    fun testCPU() = runBlocking {
//        val buildUseCase = AutoGenerateSudokuUseCase(IntermediateMatrix())
//        val stage = runBlocking { buildUseCase().first() }
//        val playUseCase = PlaySudokuUseCaseImpl(stage, 2000)
//        playUseCase().collect {
//            println(it)
//            println(stage)
//        }
//
//        assertEquals(true, stage.isSudokuClear())
//    }

    @Test
    fun testCPU2() = runBlocking {
        val matrix = IntermediateMatrix(
            matrix = listOf(
                listOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
                listOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
                listOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
                listOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
                listOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
                listOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
                listOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
                listOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
                listOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
            )
        )
        val buildUseCase = AutoGenerateSudokuUseCase(matrix)
        val stage = runBlocking { buildUseCase().first() }
//        val stageBuilder = StageBuilderImpl()
//        stageBuilder.setBox(3, 3)
////        stageBuilder.setStage(
////            listOf(
////                listOf(7,0,0,1,4,0,0,0,8),
////                listOf(0,2,9,3,0,0,0,0,7),
////                listOf(0,1,0,5,0,0,4,6,0),
////                listOf(8,0,1,0,6,3,5,0,0),
////                listOf(0,9,0,2,0,0,0,8,0),
////                listOf(0,0,7,9,0,0,6,0,1),
////                listOf(0,6,4,0,0,1,0,7,0),
////                listOf(5,0,0,0,0,7,3,1,0),
////                listOf(1,0,0,0,2,5,0,0,6)
////            )
////        )
////        stageBuilder.setStage(
////            listOf(
////                listOf(0,0,3,7,0,4,5,0,0),
////                listOf(0,2,0,0,0,0,0,6,0),
////                listOf(0,8,0,3,1,6,0,2,0),
////                listOf(0,0,0,0,0,0,0,0,0),
////                listOf(3,7,0,0,0,0,0,9,2),
////                listOf(2,0,4,0,0,0,8,0,6),
////                listOf(0,4,0,1,3,5,0,7,0),
////                listOf(0,5,0,0,0,0,0,4,0),
////                listOf(0,0,1,6,0,7,2,0,0)
////            )
////        )
//        stageBuilder.setStage(
//            listOf(
//                listOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
//                listOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
//                listOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
//                listOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
//                listOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
//                listOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
//                listOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
//                listOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
//                listOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
//            )
//        )
//        val stage = stageBuilder.build()


//        stage.setValueChangedListener(object : IntCoordinateCellEntity.ValueChangedListener {
//            override fun onChanged(cell: IntCoordinateCellEntity) {
//                println(cell)
//                println(stage)
//            }
//        })
//        val cpu = AutoPlayStageImpl(stage, 0)
//        cpu.play()

        val playUseCase = PlaySudokuUseCaseImpl(stage, 0)
        playUseCase().collect()

        println(stage)

        assertEquals(true, stage.isSudokuClear())
    }
}