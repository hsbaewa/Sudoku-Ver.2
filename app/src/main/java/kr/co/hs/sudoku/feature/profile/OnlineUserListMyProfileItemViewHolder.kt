package kr.co.hs.sudoku.feature.profile

import coil.request.Disposable
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemUserBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage

class OnlineUserListMyProfileItemViewHolder(val binding: LayoutListItemUserBinding) :
    OnlineUserListItemViewHolder<OnlineUserListItem.UserForMe>(binding.root) {

    private var disposableIcon: Disposable? = null

    fun onRecycled() {
        disposableIcon?.dispose()
    }

    override fun onBind(item: OnlineUserListItem.UserForMe) {
        with(binding) {
            tvDisplayName.text = item.profile.displayName
            disposableIcon = ivProfileIcon.loadProfileImage(
                item.profile.iconUrl,
                R.drawable.ic_person
            )
            tvFlag.text = item.profile.locale?.getLocaleFlag() ?: ""
        }
    }
}