package kr.co.hs.sudoku.feature.multi.play

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.stage.ControlStageFragment
import kr.co.hs.sudoku.model.battle.ParticipantEntity

class MultiPlayControlStageFragment : ControlStageFragment(), MultiPlayStage {

    private val viewModel: MultiPlayViewModel by activityViewModels()

    override fun onCellValueChangedListener(row: Int, column: Int, value: Int?): Boolean {
        setValue(row, column, value ?: 0)
        viewLifecycleOwner.lifecycleScope
            .launch(CoroutineExceptionHandler { _, _ -> })
            { viewModel.doUpdateMatrix(row, column, value ?: 0) }
        return true
    }

    override fun onClickFunctionButton() = viewModel.toggleReadyOrStart()

    override fun setStatus(participant: ParticipantEntity) = when (participant) {
        is ParticipantEntity.Cleared ->
            setStatus(false, null)

        is ParticipantEntity.Guest ->
            setStatus(false, getString(R.string.caption_pending_battle_for_participant))

        is ParticipantEntity.Host ->
            setStatus(false, getString(R.string.caption_start_battle_for_host))

        is ParticipantEntity.Playing -> {
            setStatus(true, null)
            setValues(participant.matrix)
        }

        is ParticipantEntity.ReadyGuest ->
            setStatus(false, getString(R.string.caption_ready_battle_for_participant))
    }
}