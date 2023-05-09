package kr.co.hs.sudoku.model.battle

import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity

data class BattleParticipantEntity(
    override val uid: String,
    override var displayName: String,
    override var message: String?,
    override var iconUrl: String?,
    override val locale: LocaleEntity?,
    val matrix: IntMatrix,
    val clearTime: Long,
    val isReady: Boolean
) : ProfileEntity