package kr.co.hs.sudoku.repository.settings

import kotlinx.coroutines.flow.Flow
import kr.co.hs.sudoku.model.settings.GameSettingsEntity

interface GameSettingsRepository {
    fun getGameSettings(): Flow<GameSettingsEntity>
    suspend fun setGameSettings(entity: GameSettingsEntity)
}