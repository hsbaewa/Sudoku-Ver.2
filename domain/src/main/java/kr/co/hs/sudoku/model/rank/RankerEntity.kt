package kr.co.hs.sudoku.model.rank

import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity

data class RankerEntity(
    override val uid: String,
    override var displayName: String,
    override var message: String?,
    override var iconUrl: String?,
    override var locale: LocaleEntity?,
    val rank: Long,
    val clearTime: Long
) : ProfileEntity, Comparable<RankerEntity> {
    constructor(profile: ProfileEntity, clearTime: Long) : this(
        profile.uid,
        profile.displayName,
        profile.message,
        profile.iconUrl,
        profile.locale,
        -1,
        clearTime
    )

    override fun compareTo(other: RankerEntity) = clearTime.compareTo(other.clearTime)
}