package kr.co.hs.sudoku.feature.battle

import kr.co.hs.sudoku.model.battle.ParticipantEntity

interface MultiPlayStage {
    fun setStatus(participant: ParticipantEntity)
}