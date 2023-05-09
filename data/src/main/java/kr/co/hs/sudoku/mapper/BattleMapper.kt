package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsModel
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kotlin.math.sqrt

object BattleMapper {
    fun BattleModel.toDomain(): BattleEntity? {

        val id = this.id ?: return null
        var columnCount: Int
        var startingMatrix: List<List<Int>> = emptyList()

        this.startingMatrix?.run {
            columnCount = sqrt(size.toDouble()).toInt()
            startingMatrix = toMatrix(columnCount)
        } ?: return null

        val createdAt = createdAt?.toDate() ?: return null

        val winner = winnerUid
        val startedAt = this.startedAt

        return when {
            winner != null -> BattleEntity.ClearedBattleEntity(
                id, hostUid, startingMatrix, createdAt, winner
            )

            startedAt != null -> BattleEntity.RunningBattleEntity(
                id, hostUid, startingMatrix, createdAt, startedAt.toDate()
            )

            else -> BattleEntity.WaitingBattleEntity(id, hostUid, startingMatrix, createdAt)
        }
    }

    private fun List<Int>.toMatrix(columnCount: Int) = List(columnCount) { row ->
        List(columnCount) { column -> this[(row * columnCount) + column] }
    }

    fun BattleParticipantModel.toDomain(): BattleParticipantEntity {
        val matrix = this.matrix?.run {
            val columnCount = sqrt(size.toDouble()).toInt()
            toMatrix(columnCount)
        } ?: emptyList()
        return BattleParticipantEntity(
            uid = uid,
            displayName = name,
            message = message,
            iconUrl = iconUrl,
            locale = locale.toDomain(),
            matrix = CustomMatrix(matrix),
            clearTime = clearTime ?: -1,
            isReady = isReady
        )
    }

    fun BattleStatisticsModel.toDomain() = BattleStatisticsEntity(
        uid = uid,
        clearedCount = clearCount,
        winCount = winCount
    )
}