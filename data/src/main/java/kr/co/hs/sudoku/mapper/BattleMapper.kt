package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantEntity
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsModel
import kr.co.hs.sudoku.model.battle2.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kotlin.math.sqrt

@Suppress("unused")
object BattleMapper {
    fun BattleModel.toDomain(): kr.co.hs.sudoku.model.battle.BattleEntity? {

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
            startedAt != null && winner != null -> kr.co.hs.sudoku.model.battle.BattleEntity.ClearedBattleEntity(
                id,
                hostUid,
                startingMatrix,
                createdAt,
                startedAt.toDate(),
                winner,
                startingParticipants.size,
                participantSize
            )

            startedAt != null -> kr.co.hs.sudoku.model.battle.BattleEntity.RunningBattleEntity(
                id,
                hostUid,
                startingMatrix,
                createdAt,
                startedAt.toDate(),
                startingParticipants.size,
                participantSize
            )

            pendingAt != null -> kr.co.hs.sudoku.model.battle.BattleEntity.PendingBattleEntity(
                id,
                hostUid,
                startingMatrix,
                createdAt,
                pendingAt.toDate(),
                startingParticipants.size,
                participantSize
            )


            else -> kr.co.hs.sudoku.model.battle.BattleEntity.WaitingBattleEntity(
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

//        return when {
//            !isReady -> BattleParticipantEntity.UnReady(
//                uid = uid,
//                displayName = name,
//                message = message,
//                iconUrl = iconUrl,
//                locale = locale.toDomain()
//            )
//
//            isReady -> BattleParticipantEntity.Ready(
//                uid = uid,
//                displayName = name,
//                message = message,
//                iconUrl = iconUrl,
//                locale = locale.toDomain(),
//                matrix = CustomMatrix(matrix)
//            )
//
//            clearTime != null -> BattleParticipantEntity.Cleared(
//                uid = uid,
//                displayName = name,
//                message = message,
//                iconUrl = iconUrl,
//                locale = locale.toDomain(),
//                matrix = CustomMatrix(matrix),
//                clearTime = clearTime ?: -1
//            )
//
//            else -> BattleParticipantEntity.Invalid(
//                uid = uid,
//                displayName = name,
//                message = message,
//                iconUrl = iconUrl,
//                locale = locale.toDomain()
//            )
//        }
    }

    fun BattleStatisticsModel.toDomain() = BattleStatisticsEntity(
        uid = uid,
        clearedCount = clearCount,
        winCount = winCount
    )


    fun BattleModel.toDomain2() = runCatching {
        val id = id
            ?: throw Exception("invalid battle model's id")
        val matrix = startingMatrix
            ?.run { toMatrix(sqrt(size.toDouble()).toInt()) }
            ?.run { CustomMatrix(this) }
            ?: throw Exception("invalid battle model's starting matrix")
        val createdAt = createdAt
            ?.toDate()
            ?: throw Exception("invalid battle model's createAt")

        when {
            startedAt != null && winnerUid != null -> kr.co.hs.sudoku.model.battle2.BattleEntity.Closed(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize,
                winner = winnerUid ?: throw Exception("invalid battle model's winner uid")
            )

            startedAt != null -> kr.co.hs.sudoku.model.battle2.BattleEntity.Playing(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize,
                playedAt = startedAt?.toDate()
                    ?: throw Exception("invalid battle model's started at")
            )

            pendingAt != null -> kr.co.hs.sudoku.model.battle2.BattleEntity.Pending(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize,
                pendedAt = pendingAt?.toDate()
                    ?: throw Exception("invalid battle model's pended at")
            )

            else -> kr.co.hs.sudoku.model.battle2.BattleEntity.Opened(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize
            )
        }
    }.getOrElse { kr.co.hs.sudoku.model.battle2.BattleEntity.Invalid }

    fun BattleParticipantModel.toDomain2(battle: kr.co.hs.sudoku.model.battle2.BattleEntity): ParticipantEntity {
        val matrix = this.matrix?.run {
            val columnCount = sqrt(size.toDouble()).toInt()
            toMatrix(columnCount)
        } ?: emptyList()

        return when (battle) {
            is kr.co.hs.sudoku.model.battle2.BattleEntity.Opened -> when {
                uid == battle.host -> ParticipantEntity.Host(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain()
                )

                isReady -> ParticipantEntity.ReadyGuest(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain()
                )

                else -> ParticipantEntity.Guest(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain()
                )
            }

            is kr.co.hs.sudoku.model.battle2.BattleEntity.Pending -> when (uid) {
                battle.host -> ParticipantEntity.Host(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain()
                )

                else -> ParticipantEntity.ReadyGuest(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain()
                )
            }

            is kr.co.hs.sudoku.model.battle2.BattleEntity.Playing -> clearTime
                ?.let { record ->
                    ParticipantEntity.Cleared(
                        uid = uid,
                        displayName = name,
                        message = message,
                        iconUrl = iconUrl,
                        locale = locale.toDomain(),
                        matrix = CustomMatrix(matrix),
                        record = record
                    )
                }
                ?: ParticipantEntity.Playing(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain(),
                    matrix = CustomMatrix(matrix)
                )

            is kr.co.hs.sudoku.model.battle2.BattleEntity.Closed -> clearTime
                ?.let { record ->
                    ParticipantEntity.Cleared(
                        uid = uid,
                        displayName = name,
                        message = message,
                        iconUrl = iconUrl,
                        locale = locale.toDomain(),
                        matrix = CustomMatrix(matrix),
                        record = record
                    )
                }
                ?: ParticipantEntity.Playing(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain(),
                    matrix = CustomMatrix(matrix)
                )

            kr.co.hs.sudoku.model.battle2.BattleEntity.Invalid -> ParticipantEntity.Guest(
                uid = uid,
                displayName = name,
                message = message,
                iconUrl = iconUrl,
                locale = locale.toDomain()
            )
        }

    }
}