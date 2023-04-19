package kr.co.hs.sudoku.model.rank

import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.ProfileEntityImpl

data class RankerEntity(
    override val uid: String,
    override var displayName: String,
    override var message: String?,
    override var iconUrl: String?,
    override var locale: LocaleEntity?,
    val clearTime: Long
) : ProfileEntity, Comparable<RankerEntity> {
    constructor(
        uid: String,
        displayName: String,
        clearTime: Long
    ) : this(
        uid,
        displayName,
        null,
        null,
        null,
        clearTime
    )

    constructor(profile: ProfileEntity, clearTime: Long) : this(
        profile.uid,
        profile.displayName,
        profile.message,
        profile.iconUrl,
        profile.locale,
        clearTime
    )

    override fun compareTo(other: RankerEntity) = clearTime.compareTo(other.clearTime)

    fun toProfile(): ProfileEntity = ProfileEntityImpl(uid, displayName, message, iconUrl, locale)
}