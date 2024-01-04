package kr.co.hs.sudoku.feature.ad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import kr.co.hs.sudoku.BuildConfig
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayNativeAdBinding
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData

class NativeAdFragment : Fragment() {
    companion object {
        fun newInstance() = NativeAdFragment()
    }

    private val adUnitId: String?
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            requireContext().getMetaData("kr.co.hs.sudoku.adUnitId.NativeAdForStage")
                ?.takeIf { it.isNotEmpty() }
        }
    private lateinit var binding: LayoutPlayNativeAdBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutPlayNativeAdBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAdLoader()
            ?.forNativeAd {
                with(binding.nativeAdView) {
                    iconView = binding.ivIcon
                    headlineView = binding.tvHeadline
                    bodyView = binding.tvBody
                    mediaView = binding.mediaView
                    callToActionView = binding.layout
                }

                it.mediaContent?.run {
                    binding.mediaView.mediaContent = this
                    binding.mediaView.isVisible = true
                }
                it.icon?.drawable?.run { binding.ivIcon.setImageDrawable(this) }
                it.headline?.run { binding.tvHeadline.text = this }
                it.body?.run { binding.tvBody.text = this }

                binding.nativeAdView.setNativeAd(it)
                binding.progressCircular.isVisible = false
            }
            ?.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    binding.progressCircular.isVisible = false
                }
            })
            ?.build()
            ?.loadAd(AdRequest.Builder().build())
            ?: run { binding.progressCircular.isVisible = false }
    }


    private fun getAdLoader(): AdLoader.Builder? {
        val adUnitId = adUnitId ?: return null

        return AdLoader.Builder(requireContext(), adUnitId)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
//                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                    .build()
            )
    }
}