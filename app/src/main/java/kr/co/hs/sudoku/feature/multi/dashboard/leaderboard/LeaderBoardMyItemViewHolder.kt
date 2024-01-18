package kr.co.hs.sudoku.feature.multi.dashboard.leaderboard

import coil.load
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemMultiLeaderboardMyRankBinding
import kr.co.hs.sudoku.feature.UserProfileViewModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.viewmodel.ViewModel

class LeaderBoardMyItemViewHolder(
    private val binding: LayoutListItemMultiLeaderboardMyRankBinding,
    private val profileViewModel: UserProfileViewModel
) : LeaderBoardItemViewHolder<LeaderBoardItem.MyItem>(binding.root) {
    override fun onBind(item: LeaderBoardItem.MyItem) {
        with(binding) {
            tvRank.text = getRankingText(item.entity.ranking)
            item.entity.takeIf { it.uid.isNotEmpty() }
                ?.let { entity ->
                    tvRecord.text = getStatisticsText(item.entity)
                    requestProfileJob = profileViewModel.requestProfile(entity.uid, onResultProfile)
                }
                ?: run {
                    tvRecord.text = "-"
                    setProfile(null)
                }

            tvDisplayName.setTextColor(getColorCompat(R.color.black))
        }
    }

    private fun setProfile(profileEntity: ProfileEntity?) = with(binding) {
        tvDisplayName.text = profileEntity?.displayName ?: "-"
        profileEntity?.iconUrl
            ?.run { ivProfileIcon.load(this) { crossfade(true) } }
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