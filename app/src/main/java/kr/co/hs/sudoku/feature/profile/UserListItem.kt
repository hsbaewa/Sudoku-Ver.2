package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.model.user.ProfileEntity

sealed class UserListItem {
    abstract val id: Int

    data class Header(val header: String) : UserListItem() {
        override val id: Int
            get() = header.hashCode()
    }

    data class User(val profile: ProfileEntity) : UserListItem() {
        override val id: Int
            get() = profile.uid.hashCode()
    }

    object EmptyMessage : UserListItem() {
        override val id: Int
            get() = "empty".hashCode()
    }
}