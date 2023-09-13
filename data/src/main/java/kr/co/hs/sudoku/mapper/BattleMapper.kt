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

@Suppress("unused")
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
        val pendingAt = this.pendingAt

        return when {
            startedAt != null && winner != null -> BattleEntity.ClearedBattleEntity(
                id,
                hostUid,
                startingMatrix,
                createdAt,
                startedAt.toDate(),
                winner,
                startingParticipants.size,
                participantSize
            )

            startedAt != null -> BattleEntity.RunningBattleEntity(
                id,
                hostUid,
                startingMatrix,
                createdAt,
                startedAt.toDate(),
                startingParticipants.size,
                participantSize
            )

            pendingAt != null -> BattleEntity.PendingBattleEntity(
                id,
                hostUid,
                startingMatrix,
                createdAt,
                pendingAt.toDate(),
                startingParticipants.size,
                participantSize
            )


            else -> BattleEntity.WaitingBattleEntity(
                id, hostUid, startingMatrix, createdAt, startingParticipants.size, participantSize
            )
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