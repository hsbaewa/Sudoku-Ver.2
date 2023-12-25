package kr.co.hs.sudoku.feature.singleplay

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.stage.ControlStageFragment
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class SinglePlayControlStageFragment : ControlStageFragment() {

    private val singlePlayViewModel: SinglePlayViewModel by activityViewModels()
    private val recordViewModel: RecordViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            recordViewModel.cellEventHistoryFlow.collect { item ->
                when (item) {
                    is HistoryItem.Removed -> setValue(item.row, item.column, 0)
                    is HistoryItem.Set -> setValue(item.row, item.column, item.value)
                }
            }
        }
        singlePlayViewModel.command.observe(viewLifecycleOwner) {
            when (it) {
                is SinglePlayViewModel.Matrix -> {
                    initBoard()
                    setStatus(false, getString(R.string.start))
                }

                is SinglePlayViewModel.Created -> {
                    setStatus(false, null)
                    startCountDown { singlePlayViewModel.start() }
                }

                is SinglePlayViewModel.Started -> {
                    setStatus(true, null)
                    setValues(it.stage)
                }

                SinglePlayViewModel.StartReplay -> clearBoard()
            }
        }
    }

    override fun onClickFunctionButton() {
        super.onClickFunctionButton()
        singlePlayViewModel.create()
    }

    override fun onCellTouchDown(row: Int, column: Int) =
        if (recordViewModel.isRunningCapturedHistoryEvent() || isCleared()) {
            false
        } else {
            super.onCellTouchDown(row, column)
        }
}