package kr.co.hs.sudoku.usecase.battle

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository

class BattleMonitorUseCase(
    private val repository: BattleRepository
) : BattleRepository.BattleChangedListener {

    private val sharedFlow = MutableSharedFlow<BattleEntity>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    operator fun invoke(battle: BattleEntity) = sharedFlow
        .onStart { repository.bindBattle(battle.id, this@BattleMonitorUseCase) }
        .onCompletion { repository.unbindBattle(battle.id) }

    override fun onChanged(battle: BattleEntity) {
        sharedFlow.tryEmit(battle)
    }
}