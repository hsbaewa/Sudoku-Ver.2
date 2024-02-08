package kr.co.hs.sudoku.feature.ad

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kr.co.hs.sudoku.BuildConfig
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData

class AppOpenAdManager(
    private val activity: Activity
) : AppOpenAd.AppOpenAdLoadCallback(), ViewTreeObserver.OnPreDrawListener {

    private var loadAdError: LoadAdError? = null
    private val adUnitId: String?
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9257395921"
        } else {
            activity.getMetaData("kr.co.hs.sudoku.adUnitId.OpenAd")
                ?.takeIf { it.isNotEmpty() }
        }

    private val splashContent: View
        get() = activity.findViewById(android.R.id.content)

    private var adClicked = false
    private var adDismissed = false
    private var adShowed = false
    private var adError: AdError? = null
    private var adImpressed = false

    override fun onAdLoaded(p0: AppOpenAd) {
        p0.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                adClicked = true
            }

            override fun onAdDismissedFullScreenContent() {
                adDismissed = true
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                adError = p0
            }

            override fun onAdImpression() {
                adImpressed = true
            }

            override fun onAdShowedFullScreenContent() {
                adShowed = true
            }
        }
        p0.show(activity)
    }


    override fun onAdFailedToLoad(p0: LoadAdError) {
        this.loadAdError = p0
    }

    fun showIfAvailable() {
        val adUnitId = this.adUnitId ?: return

        // Set up an OnPreDrawListener to the root view.
        splashContent.viewTreeObserver.addOnPreDrawListener(this)

        val request = AdRequest.Builder().build()
        AppOpenAd.load(activity, adUnitId, request, this)
    }

    override fun onPreDraw(): Boolean {
        // Check whether the initial data is ready.
        return if (loadAdError != null || adError != null || adDismissed) {
            // The content is ready. Start drawing.
            splashContent.viewTreeObserver.removeOnPreDrawListener(this)
            onAdDismissedListener?.onDismissed()
            true
        } else {
            // The content isn't ready. Suspend.
            false
        }
    }

    interface OnAdDismissedListener {
        fun onDismissed() {}
    }

    private var onAdDismissedListener: OnAdDismissedListener? = null
    fun setOnAdDismissedListener(l: OnAdDismissedListener) {
        this.onAdDismissedListener = l
    }
}