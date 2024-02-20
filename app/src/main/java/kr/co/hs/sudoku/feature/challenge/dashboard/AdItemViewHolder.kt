package kr.co.hs.sudoku.feature.challenge.dashboard

import kr.co.hs.sudoku.databinding.LayoutListItemChallengeAdBinding

class AdItemViewHolder(private val binding: LayoutListItemChallengeAdBinding) :
    ChallengeDashboardListItemViewHolder<ChallengeDashboardListItem.AdItem>(binding.root) {
    override fun onBind(item: ChallengeDashboardListItem) {
        when (item) {
            is ChallengeDashboardListItem.AdItem -> {
                with(binding.nativeAdView) {
                    headlineView = binding.tvTitle
                    bodyView = binding.tvDescription
                    mediaView = binding.mediaView
                }

                item.nativeAd.mediaContent?.run { binding.mediaView.mediaContent = this }
                item.nativeAd.headline?.run { binding.tvTitle.text = this }
                item.nativeAd.body?.run { binding.tvDescription.text = this }

                binding.nativeAdView.setNativeAd(item.nativeAd)
            }

            else -> {}
        }
    }

    override fun onViewAttachedToWindow() {}
    override fun onViewDetachedFromWindow() {}
    override fun onViewRecycled() {}
}