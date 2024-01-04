package kr.co.hs.sudoku.feature.ad

import android.app.Activity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kr.co.hs.sudoku.BuildConfig
import kr.co.hs.sudoku.extension.platform.ActivityExtension.screenWidth
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData

class AdaptiveBannerAdManager(
    private val activity: Activity
) : ViewTreeObserver.OnGlobalLayoutListener {

    private lateinit var container: ViewGroup
    private val adUnitId: String?
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9214589741"
        } else {
            activity.getMetaData("kr.co.hs.sudoku.adUnitId.BannerAdSinglePlay")
                ?.takeIf { it.isNotEmpty() }
        }

    fun attachBanner(container: ViewGroup) {
        this.container = container
        container.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        if (this::container.isInitialized) {
            this.container.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
        loadBanner()
    }

    private fun loadBanner() {
        if (!this::container.isInitialized) {
            return
        }

        val adUnitId = this@AdaptiveBannerAdManager.adUnitId ?: return

        val adView = AdView(activity)
        container.addView(adView)
        with(adView) {
            this.adUnitId = adUnitId

            val density = resources.displayMetrics.density
            val adWidth = activity.screenWidth / density

            setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth.toInt())
            )

            loadAd(AdRequest.Builder().build())
        }
    }

}