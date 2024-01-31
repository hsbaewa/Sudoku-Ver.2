package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.model.user.ProfileEntity

sealed class OnlineUserListItem {
    abstract val id: Int

    data class Header(val header: String) : OnlineUserListItem() {
        override val id: Int
            get() = header.hashCode()
    }

    data class User(val profile: ProfileEntity) : OnlineUserListItem() {
        override val id: Int
            get() = profile.uid.hashCode()
    }

    object EmptyMessage : OnlineUserListItem() {
        override val id: Int
            get() = "empty".hashCode()
    }
}