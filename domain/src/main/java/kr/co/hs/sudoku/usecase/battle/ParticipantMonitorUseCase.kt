package kr.co.hs.sudoku.usecase.battle

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository

class ParticipantMonitorUseCase(
    private val repository: BattleRepository
) : BattleRepository.ParticipantChangedListener {

    private val sharedFlow = MutableSharedFlow<BattleParticipantEntity>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    operator fun invoke(battle: BattleEntity, uid: String) = invoke(battle.id, uid)

    operator fun invoke(battleId: String, uid: String) = sharedFlow
        .onStart { repository.bindParticipant(battleId, uid, this@ParticipantMonitorUseCase) }
        .onCompletion { repository.unbindParticipant(battleId, uid) }

    override fun onChanged(participant: BattleParticipantEntity) {
        sharedFlow.tryEmit(participant)
    }

}