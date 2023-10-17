package kr.co.hs.sudoku.repository.battle2

import kotlinx.coroutines.flow.SharedFlow
import kr.co.hs.sudoku.model.battle2.BattleEntity

interface BattleEventRepository {
    val battleId: String
    val battleFlow: SharedFlow<BattleEntity>
    fun getBattle(): BattleEntity

    fun startMonitoring()
    fun stopMonitoring()
}