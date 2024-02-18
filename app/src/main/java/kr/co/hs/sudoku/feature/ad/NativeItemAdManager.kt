package kr.co.hs.sudoku.feature.ad

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NativeItemAdManager(
    private val context: Context,
    private val adUnitId: String?
) {

    private fun getAdLoader(): AdLoader.Builder? {
        val adUnitId = adUnitId ?: return null

        return AdLoader.Builder(context, adUnitId)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
//                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                    .build()
            )
    }

    suspend fun fetchNativeAd() = suspendCoroutine { emit ->
        getAdLoader()
            ?.forNativeAd { emit.resume(it) }
            ?.withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        emit.resume(null)
                    }
                }
            )
            ?.build()
            ?.loadAd(AdRequest.Builder().build())
            ?: emit.resume(null)
    }
}