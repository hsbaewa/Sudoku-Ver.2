package kr.co.hs.sudoku.feature.single

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.stage.ControlStageFragment
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.model.stage.history.impl.HistoryQueueImpl
import kr.co.hs.sudoku.repository.timer.TimerImpl
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class SinglePlayControlStageFragment : ControlStageFragment() {

    private val viewModel: SinglePlayViewModel by activityViewModels()
    private val recordViewModel: RecordViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.stage.observe(viewLifecycleOwner) {
            it?.run {
                startPending(this)
            } ?: run {
                clearAllStageValues()
                setStatus(false, getString(R.string.start))
            }
        }
    }

    override fun onClickFunctionButton() {
        super.onClickFunctionButton()
        viewModel.createStage()
    }

    private fun startPending(stage: Stage) {
        setStatus(false, null)
        startCountDown { startGame(stage) }
    }

    private fun startGame(stage: Stage) {
        setStatus(true, null)
        setValues(stage.toValueTable())

        with(recordViewModel) {
            bindStage(this)
            setTimer(TimerImpl())
            setHistoryWriter(HistoryQueueImpl())
        }

        recordViewModel.play()
    }
}