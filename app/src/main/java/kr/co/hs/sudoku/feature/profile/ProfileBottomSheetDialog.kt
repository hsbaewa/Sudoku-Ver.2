package kr.co.hs.sudoku.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutDialogUserProfileBinding
import kr.co.hs.sudoku.extension.CoilExt.loadProfileImage
import kr.co.hs.sudoku.extension.platform.FragmentExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardViewModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileBottomSheetDialog : BottomSheetDialogFragment() {
    companion object {

        private const val EXTRA_USER_ID = "EXTRA_USER_ID"
        fun show(fragmentManager: FragmentManager, uid: String) =
            ProfileBottomSheetDialog()
                .apply { arguments = bundleOf(EXTRA_USER_ID to uid) }
                .show(fragmentManager, ProfileBottomSheetDialog::class.java.name)
    }

    private lateinit var binding: LayoutDialogUserProfileBinding
    private val profileViewModel: UserProfileViewModel
            by viewModels { (requireActivity() as Activity).getUserProfileProviderFactory() }
    private val multiDashboardViewModel: MultiDashboardViewModel by viewModels {
        val app = requireContext().applicationContext as App
        MultiDashboardViewModel.ProviderFactory(app.getBattleRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogUserProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileViewModel.isRunningProgress.observe(viewLifecycleOwner) {
            isShowProgressIndicator =
                it || (multiDashboardViewModel.isRunningProgress.value == true)
        }
        multiDashboardViewModel.isRunningProgress.observe(viewLifecycleOwner) {
            isShowProgressIndicator = it || (profileViewModel.isRunningProgress.value == true)
        }

        binding.tvLastChecked.text = "-"
        profileViewModel.profile.observe(viewLifecycleOwner) {
            it?.iconUrl?.run { binding.ivIcon.loadProfileImage(this, R.drawable.ic_person) }
            binding.tvDisplayName.text = it?.displayName?.takeIf { it.isNotEmpty() } ?: "-"
            binding.tvMessage.text = it?.message?.takeIf { it.isNotEmpty() } ?: "-"
            binding.tvNation.text = it?.locale?.getLocaleFlag() ?: ""
            binding.tvLastChecked.text = when (it) {
                is ProfileEntity.OnlineUserEntity -> SimpleDateFormat(
                    getString(R.string.profile_last_checked_format),
                    Locale.getDefault()
                ).format(it.checkedAt)

                else -> "-"
            }
        }

        arguments?.getString(EXTRA_USER_ID)?.let { uid ->
            multiDashboardViewModel.requestStatistics(uid) {
                when (it) {
                    is ViewModel.OnStart -> {}
                    is ViewModel.OnError -> {}
                    is ViewModel.OnFinish -> {
                        binding.tvMultiPlayRank.text = getString(
                            R.string.format_statistics_with_ranking,
                            it.d.winCount,
                            it.d.playCount - it.d.winCount,
                            when (it.d.ranking) {
                                0L -> getString(R.string.rank_format_nan)
                                1L -> getString(R.string.rank_format_first)
                                2L -> getString(R.string.rank_format_second)
                                3L -> getString(R.string.rank_format_third)
                                else -> getString(R.string.rank_format, it.d.ranking)
                            }
                        )
                    }

                }
            }

            with(profileViewModel) {
                requestProfile(uid)
            }
        }
    }
}