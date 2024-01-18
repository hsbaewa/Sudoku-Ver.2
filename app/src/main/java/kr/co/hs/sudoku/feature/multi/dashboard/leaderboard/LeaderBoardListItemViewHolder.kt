package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemMultiLeaderboardRankBinding
import kr.co.hs.sudoku.feature.UserProfileViewModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.viewmodel.ViewModel

class LeaderBoardListItemViewHolder(
    private val binding: LayoutListItemMultiLeaderboardRankBinding,
    private val profileViewModel: UserProfileViewModel
) : LeaderBoardItemViewHolder<LeaderBoardItem.ListItem>(binding.root) {
    override fun onBind(item: LeaderBoardItem.ListItem) {
        with(binding) {
            tvRank.text = getRankingText(item.entity.ranking)
            item.entity.takeIf { it.uid.isNotEmpty() }
                ?.let { entity ->
                    tvRecord.text = getStatisticsText(entity)
                    requestProfileJob = profileViewModel.requestProfile(entity.uid, onResultProfile)
                }
                ?: run {
                    tvRecord.text = "-"
                    setProfile(null)
                }

            tvDisplayName.setTextColor(
                getColorCompat(
                    when (item.isMine) {
                        true -> R.color.black
                        false -> R.color.gray_600
                    }
                )
            )
        }
    }

    private fun setProfile(profileEntity: ProfileEntity?) = with(binding) {
        tvDisplayName.text = profileEntity?.displayName ?: "-"
        profileEntity?.iconUrl
            ?.run { ivProfileIcon.loadProfileImage(this) }
            ?: ivProfileIcon.setImageDrawable(null)
    }

    private val onResultProfile: (ViewModel.RequestStatus<ProfileEntity>) -> Unit = {
        when (it) {
            is ViewModel.OnStart -> {}
            is ViewModel.OnError -> {}
            is ViewModel.OnFinish -> setProfile(it.d)
        }
    }

}