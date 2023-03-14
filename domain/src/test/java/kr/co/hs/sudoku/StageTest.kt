package kr.co.hs.sudoku

import kotlinx.coroutines.runBlocking
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.MutableStage
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.impl.AutoPlayStageImpl
import kr.co.hs.sudoku.model.stage.impl.IntCoordinateCellEntityImpl
import kr.co.hs.sudoku.model.stage.impl.MutableStageImpl
import kr.co.hs.sudoku.model.stage.impl.StageBuilderImpl
import org.junit.Assert.*
import org.junit.Test

class StageTest : IntCoordinateCellEntity.ValueChangedListener {

    private var cell: IntCoordinateCellEntity? = null

    @Test
    fun testMutableSudokuStageMod1() {
        val table = List(9) { row ->
            MutableList<IntCoordinateCellEntity>(9) { column ->
                IntCoordinateCellEntityImpl(row, column)
            }
        }
        val sudoku: MutableStage = MutableStageImpl(3, 3, table)
        sudoku.setValueChangedListener(this)

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
        sudoku.setValueChangedListener(this)
        assertEquals(16, sudoku.getEmptyCellCount())
        assertEquals(0, sudoku.getDuplicatedCellCount())

        var box = sudoku.getBox(0, 0)
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
        assertEquals(true, sudoku.getBox(0, 0).isCompleted())

        sudoku[0, 2] = 5
        assertEquals(true, sudoku.getCell(0, 2).isEmpty())

        sudoku[0, 2] = 1
        sudoku[0, 3] = 2
        sudoku[1, 2] = 3
        sudoku[1, 3] = 4
        assertEquals(true, sudoku.getBox(0, 1).isCompleted())

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

        assertEquals(true, sudoku.isCompleted())
    }

    override fun onChanged(cell: IntCoordinateCellEntity) {
        this.cell = cell
    }

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
        assertEquals(true, sudoku.isCompleted())
    }

    @Test
    fun testBuilder() {
        val stage = buildStage()
        assertThrows(IllegalArgumentException::class.java) {
            stage.set(0, 0, 1)
        }

        stage[0, 2] = 1
    }

    private fun buildStage(): Stage {
        val stageBuilder = StageBuilderImpl()
        stageBuilder.setBox(3, 3)
        stageBuilder.autoGenerate(
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
        stageBuilder.setStage(listOf(listOf(1, 2, 3, 4)))

        val sudoku = stageBuilder.build()

        println(sudoku)
        return sudoku
    }

    @Test
    fun testAutoFill() {
        val stageBuilder = StageBuilderImpl()
        stageBuilder.setBox(3, 3)
        stageBuilder.autoGenerate(
            listOf(
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1),
                listOf(1, 1, 1, 1, 1, 1, 1, 1, 1)
            )
        )
        val stage = stageBuilder.build()
        assertEquals(true, stage.isCompleted())
    }

    @Test
    fun testCustomFill() {
        val stageBuilder = StageBuilderImpl()
        stageBuilder.setBox(3, 3)
        stageBuilder.setStage(
            listOf(
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
        val stage = stageBuilder.build()
        assertEquals(false, stage.isCompleted())
        assertEquals(0, stage.getDuplicatedCellCount())

    }

    @Test
    fun testCPU() = runBlocking {
        val stageBuilder = StageBuilderImpl()
        stageBuilder.setBox(3, 3)
        val stage = stageBuilder.build()
        stage.setValueChangedListener(object : IntCoordinateCellEntity.ValueChangedListener {
            override fun onChanged(cell: IntCoordinateCellEntity) {
                println(cell)
                println(stage)
            }
        })
        val cpu = AutoPlayStageImpl(stage, 500)
        cpu.play()

        assertEquals(true, stage.isCompleted())
    }
}