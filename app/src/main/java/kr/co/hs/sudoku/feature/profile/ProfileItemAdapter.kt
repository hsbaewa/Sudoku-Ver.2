package kr.co.hs.sudoku.feature.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeClearLogBinding
import kr.co.hs.sudoku.databinding.LayoutListItemProfileBattleLadderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemProfileDisplayNameBinding
import kr.co.hs.sudoku.databinding.LayoutListItemProfileDividerBinding
import kr.co.hs.sudoku.databinding.LayoutListItemProfileIconBinding
import kr.co.hs.sudoku.databinding.LayoutListItemProfileLastCheckedBinding
import kr.co.hs.sudoku.databinding.LayoutListItemProfileMessageBinding

class ProfileItemAdapter(
    private val onClickItem: (ProfileItem?) -> Unit
) : PagingDataAdapter<ProfileItem, ProfileItemViewHolder>(ProfileItemDiffCallback()) {

    companion object {
        private const val VT_ICON = 1
        private const val VT_DISPLAY_NAME = 2
        private const val VT_MESSAGE = 3
        private const val VT_LAST_CHECKED = 4
        private const val VT_BATTLE_LADDER = 5
        private const val VT_DIVIDER = 10
        private const val VT_CHALLENGE_LOG = 11
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_ICON -> ProfileIconItemViewHolder(
                LayoutListItemProfileIconBinding.inflate(inflater, parent, false)
            )

            VT_DISPLAY_NAME -> ProfileNameItemViewHolder(
                LayoutListItemProfileDisplayNameBinding.inflate(inflater, parent, false)
            )

            VT_MESSAGE -> ProfileMessageItemViewHolder(
                LayoutListItemProfileMessageBinding.inflate(inflater, parent, false)
            )

            VT_LAST_CHECKED -> ProfileCheckedDateItemViewHolder(
                LayoutListItemProfileLastCheckedBinding.inflate(inflater, parent, false)
            )

            VT_BATTLE_LADDER -> BattleLadderItemViewHolder(
                LayoutListItemProfileBattleLadderBinding.inflate(inflater, parent, false)
            )

            VT_DIVIDER -> ProfileDividerItemViewHolder(
                LayoutListItemProfileDividerBinding.inflate(inflater, parent, false)
            )

            VT_CHALLENGE_LOG -> ChallengeLogItemViewHolder(
                LayoutListItemChallengeClearLogBinding.inflate(inflater, parent, false)
            ).apply {
                binding.root.setOnClickListener { onClickItem(getItem(bindingAdapterPosition)) }
            }

            else -> throw Exception("invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ProfileItemViewHolder, position: Int) =
        holder.onBind(getItem(position))

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ProfileItem.Icon -> VT_ICON
            is ProfileItem.DisplayName -> VT_DISPLAY_NAME
            is ProfileItem.Message -> VT_MESSAGE
            is ProfileItem.LastChecked -> VT_LAST_CHECKED
            is ProfileItem.BattleLadder -> VT_BATTLE_LADDER
            is ProfileItem.Divider -> VT_DIVIDER
            is ProfileItem.ChallengeLog -> VT_CHALLENGE_LOG
            null -> super.getItemViewType(position)
        }
    }
}