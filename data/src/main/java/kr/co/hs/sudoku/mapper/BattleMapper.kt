package kr.co.hs.sudoku.mapper

import kr.co.hs.sudoku.mapper.ProfileMapper.toDomain
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleModel
import kr.co.hs.sudoku.model.battle.BattleParticipantModel
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsModel
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kotlin.math.sqrt

@Suppress("unused")
object BattleMapper {
    private fun List<Int>.toMatrix(columnCount: Int) = List(columnCount) { row ->
        List(columnCount) { column -> this[(row * columnCount) + column] }
    }

    fun BattleStatisticsModel.toDomain() = BattleStatisticsEntity(
        uid = uid,
        clearedCount = clearCount,
        winCount = winCount
    )

    fun BattleModel.toDomain() = runCatching {
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
            startedAt != null && winnerUid != null -> BattleEntity.Closed(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize,
                winner = winnerUid ?: throw Exception("invalid battle model's winner uid")
            )

            startedAt != null -> BattleEntity.Playing(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize,
                playedAt = startedAt?.toDate()
                    ?: throw Exception("invalid battle model's started at")
            )

            pendingAt != null -> BattleEntity.Pending(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize,
                pendedAt = pendingAt?.toDate()
                    ?: throw Exception("invalid battle model's pended at"),
                isGeneratedSudoku = isGeneratedSudoku
            )

            else -> BattleEntity.Opened(
                id = id,
                host = hostUid,
                createdAt = createdAt,
                startingMatrix = matrix,
                maxParticipants = startingParticipants.size,
                participantSize = participantSize
            )
        }
    }.getOrElse { BattleEntity.Invalid() }

    fun BattleParticipantModel.toDomain2(battle: BattleEntity): ParticipantEntity {
        val matrix = this.matrix?.run {
            val columnCount = sqrt(size.toDouble()).toInt()
            CustomMatrix(toMatrix(columnCount))
        } ?: EmptyMatrix()

        return when (battle) {
            is BattleEntity.Opened -> when {
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

            is BattleEntity.Pending -> when (uid) {
                battle.host -> ParticipantEntity.Host(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain(),
                    matrix = if (battle.isGeneratedSudoku) matrix else EmptyMatrix()
                )

                else -> ParticipantEntity.ReadyGuest(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain(),
                    matrix = if (battle.isGeneratedSudoku) matrix else EmptyMatrix()
                )
            }

            is BattleEntity.Playing -> clearTime
                ?.let { record ->
                    ParticipantEntity.Cleared(
                        uid = uid,
                        displayName = name,
                        message = message,
                        iconUrl = iconUrl,
                        locale = locale.toDomain(),
                        matrix = matrix,
                        record = record
                    )
                }
                ?: ParticipantEntity.Playing(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain(),
                    matrix = matrix
                )

            is BattleEntity.Closed -> clearTime
                ?.let { record ->
                    ParticipantEntity.Cleared(
                        uid = uid,
                        displayName = name,
                        message = message,
                        iconUrl = iconUrl,
                        locale = locale.toDomain(),
                        matrix = matrix,
                        record = record
                    )
                }
                ?: ParticipantEntity.Playing(
                    uid = uid,
                    displayName = name,
                    message = message,
                    iconUrl = iconUrl,
                    locale = locale.toDomain(),
                    matrix = matrix
                )

            is BattleEntity.Invalid -> ParticipantEntity.Guest(
                uid = uid,
                displayName = name,
                message = message,
                iconUrl = iconUrl,
                locale = locale.toDomain()
            )
        }

    }
}