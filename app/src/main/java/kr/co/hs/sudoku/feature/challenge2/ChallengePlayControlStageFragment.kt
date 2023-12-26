package kr.co.hs.sudoku.feature.challenge2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.stage.ControlStageFragment
import kr.co.hs.sudoku.model.stage.history.HistoryItem
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class ChallengePlayControlStageFragment : ControlStageFragment() {
    private val challengePlayViewModel: ChallengePlayViewModel by activityViewModels()
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
        challengePlayViewModel.command.observe(viewLifecycleOwner) {
            when (it) {
                is ChallengePlayViewModel.Matrix -> {
                    initBoard()
                    setStatus(false, getString(R.string.start))
                }

                is ChallengePlayViewModel.Created -> {
                    setStatus(false, null)
                    startCountDown { challengePlayViewModel.start() }
                }

                is ChallengePlayViewModel.Started -> {
                    setStatus(true, null)
                    setValues(it.stage)
                }

                ChallengePlayViewModel.StartReplay -> clearBoard()

                else -> {}
            }
        }
    }

    override fun onClickFunctionButton() {
        super.onClickFunctionButton()
        challengePlayViewModel.create()
    }

    override fun onCellTouchDown(row: Int, column: Int) =
        if (recordViewModel.isRunningCapturedHistoryEvent() || isCleared()) {
            false
        } else {
            super.onCellTouchDown(row, column)
        }
}