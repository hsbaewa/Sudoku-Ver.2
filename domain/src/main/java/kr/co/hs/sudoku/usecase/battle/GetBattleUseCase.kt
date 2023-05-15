package kr.co.hs.sudoku.usecase.battle

import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.repository.battle.BattleRepository

class GetBattleUseCase(private val repository: BattleRepository) {
    operator fun invoke(battleId: String) = flow {
        repository.getBattle(battleId)
            ?.run { emit(this) }
            ?: throw BattleRepository.UnknownBattleException()
    }
}