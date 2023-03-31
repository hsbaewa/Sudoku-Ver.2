package kr.co.hs.sudoku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.hs.sudoku.databinding.ActivityMainBinding
import kr.co.hs.sudoku.model.stage.CellValueEntity
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.impl.StageBuilderImpl

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        val stage = StageBuilderImpl()
            .setBox(3, 3)
            .setStage(
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
            ).build()

        (0 until stage.rowCount).forEach { row ->
            (0 until stage.columnCount).forEach { column ->
                when (stage.getCell(row, column).value) {
                    CellValueEntity.Empty -> {
                        binding.board.setEnabled(row, column, true)
                        binding.board.setCellValue(row, column, 0)
                    }
                    is CellValueEntity.Immutable -> {
                        binding.board.setEnabled(row, column, false)
                        binding.board.setCellValue(row, column, stage[row, column])
                    }
                    is CellValueEntity.Mutable -> {
                        binding.board.setEnabled(row, column, true)
                        binding.board.setCellValue(row, column, stage[row, column])
                    }
                }
            }
        }

        binding.board.cellTouchDownListener = { row, column ->
            !stage.getCell(row, column).isImmutable()
        }

        val errorCell = HashSet<Pair<Int, Int>>()

        binding.board.cellValueChangedListener = { row, column, value ->
            stage[row, column] = value ?: 0

            errorCell.forEach {
                binding.board.setError(it.first, it.second, false)
            }
            errorCell.clear()

            if (stage.getDuplicatedCellCount() > 0) {
                val cellList = stage.getDuplicatedCells().toList()

                cellList.forEach {
                    val cell = (it as IntCoordinateCellEntity)
                    val x = cell.coordinate.x
                    val y = cell.coordinate.y
                    errorCell.add(x to y)
                    binding.board.setError(x, y, true)
                }
            }

            if (stage.isCompleted()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("완료")
                    .setMessage("완료")
                    .setPositiveButton("확인") { _, _ ->
                    }
                    .setCancelable(false)
                    .show()
            }


            true
        }
    }

}