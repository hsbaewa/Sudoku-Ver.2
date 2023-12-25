package kr.co.hs.sudoku.feature.multiplay

import kr.co.hs.sudoku.model.battle.ParticipantEntity

interface MultiPlayStage {
    fun setStatus(participant: ParticipantEntity)
}