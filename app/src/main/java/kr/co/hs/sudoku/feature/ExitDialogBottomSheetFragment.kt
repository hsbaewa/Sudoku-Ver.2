package kr.co.hs.sudoku.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.BuildConfig
import kr.co.hs.sudoku.databinding.LayoutDialogExitBinding
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData
import kr.co.hs.sudoku.feature.ad.NativeItemAdManager

class ExitDialogBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager) =
            ExitDialogBottomSheetFragment()
                .apply { arguments = bundleOf() }
                .show(fragmentManager, ExitDialogBottomSheetFragment::class.java.name)
    }

    private lateinit var binding: LayoutDialogExitBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogExitBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.btnCancel) {
            setOnClickListener { dismiss() }
        }

        with(binding.btnConfirm) {
            setOnClickListener { (requireActivity() as MainActivity).finish() }
        }

        binding.progressCircular.hide()
        viewLifecycleOwner.lifecycleScope.launch {
            withStarted { loadAd() }
        }
    }

    private fun loadAd() =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, _ ->
            binding.progressCircular.hide()
            binding.btnConfirm.isEnabled = true
        }) {
            binding.progressCircular.show()
            val adUnitId = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                requireContext()
                    .getMetaData("kr.co.hs.sudoku.adUnitId.NativeAdExitPopup")
                    ?.takeIf { it.isNotEmpty() }
            }
            val nativeAdManager = NativeItemAdManager(requireContext(), adUnitId)
            val nativeAd = withContext(Dispatchers.IO) { nativeAdManager.fetchNativeAd() }
                ?: throw Exception("native ad load failed")

            with(binding.nativeAdView) {
                mediaView = binding.mediaView
                iconView = binding.ivAdIcon
                headlineView = binding.tvAdTitle
                callToActionView = binding.layoutAd
            }

            with(nativeAd) {
                mediaContent?.run { binding.mediaView.mediaContent = this }
                icon?.drawable?.run {
                    binding.ivAdIcon.setImageDrawable(this)
                    binding.ivAdIcon.isVisible = true
                }
                headline?.run { binding.tvAdTitle.text = this }
            }

            binding.nativeAdView.setNativeAd(nativeAd)

            binding.progressCircular.hide()
            binding.btnConfirm.isEnabled = true
        }
}