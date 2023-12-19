package kr.co.hs.sudoku.feature.battle

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.stage.ViewerStageFragment
import kr.co.hs.sudoku.model.battle.ParticipantEntity

class MultiPlayViewerStageFragment : ViewerStageFragment(), MultiPlayStage {

    override fun setStatus(participant: ParticipantEntity) = when (participant) {
        is ParticipantEntity.Cleared ->
            setStatus(false, getString(R.string.status_clear_battle_for_participant))

        is ParticipantEntity.Guest ->
            setStatus(false, getString(R.string.status_pending_battle_for_participant))

        is ParticipantEntity.Host ->
            setStatus(false, getString(R.string.status_host_battle_for_participant))

        is ParticipantEntity.Playing -> {
            setStatus(true, null)
            setValues(participant.matrix)
        }

        is ParticipantEntity.ReadyGuest ->
            setStatus(false, getString(R.string.status_ready_battle_for_participant))
    }
}