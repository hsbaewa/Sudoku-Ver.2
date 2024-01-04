package kr.co.hs.sudoku.feature.ad

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kr.co.hs.sudoku.BuildConfig
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChallengeRetryRewardAdManager(
    private val activity: Activity
) {
    private val adUnitId: String?
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            activity.getMetaData("kr.co.hs.sudoku.adUnitId.RewardAdChallengeRetry")
                ?.takeIf { it.isNotEmpty() }
            null
        }

    private suspend fun fetchRewardedAd() = suspendCoroutine {
        this.adUnitId
            ?.let { adUnitId ->
                val adRequest = AdRequest.Builder().build()
                RewardedAd.load(activity, adUnitId, adRequest, object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) = it.resume(null)
                    override fun onAdLoaded(p0: RewardedAd) = it.resume(p0)
                })
            }
            ?: run { it.resume(null) }
    }

    suspend fun showRewardedAd(): Boolean {
        val rewardedAd = fetchRewardedAd() ?: return false
        return suspendCoroutine { emit ->
            var hasRewarded = false
            rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {}
                override fun onAdDismissedFullScreenContent() = emit.resume(hasRewarded)
                override fun onAdFailedToShowFullScreenContent(p0: AdError) = emit.resume(false)
                override fun onAdImpression() {}
                override fun onAdShowedFullScreenContent() {}
            }
            rewardedAd.show(activity) { hasRewarded = true }
        }
    }

}