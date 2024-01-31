package kr.co.hs.sudoku.feature.profile

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity

class ProfilePopupMenu(
    context: Context,
    anchor: View,
    private val onPopupMenuItemClickListener: OnPopupMenuItemClickListener
) : PopupMenu(ContextThemeWrapper(context, R.style.Theme_HSSudoku2), anchor),
    PopupMenu.OnMenuItemClickListener {
    var profile: ProfileEntity? = null

    fun show(user: ProfileEntity) {
        profile = user
        show()
    }

    override fun show() {
        inflate(R.menu.profile)
        setOnMenuItemClickListener(this)
        super.show()
    }

    fun show(uid: String, displayName: String) {
        profile = object : ProfileEntity.UserEntity {
            override val uid: String = uid
            override var displayName: String = displayName
            override var message: String? = null
            override var iconUrl: String? = null
            override val locale: LocaleEntity? = null
        }
        show()
    }

    override fun onMenuItemClick(item: MenuItem?) = when (item?.itemId) {
        R.id.profile -> profile?.run {
            onPopupMenuItemClickListener.onClickProfile(this.uid)
            true
        } ?: false

        R.id.invite_multi_play -> profile?.run {
            onPopupMenuItemClickListener.onClickInviteMultiPlay(this.uid, this.displayName)
            true
        } ?: false

        else -> false
    }

    interface OnPopupMenuItemClickListener {
        fun onClickProfile(uid: String)
        fun onClickInviteMultiPlay(uid: String, displayName: String)
    }
}