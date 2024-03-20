package kr.co.hs.sudoku.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.core.PagingLoadStateAdapter
import kr.co.hs.sudoku.databinding.LayoutDialogUserProfileBinding
import kr.co.hs.sudoku.di.user.UserModule
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.challenge.ChallengeItemBottomSheetDialog
import kr.co.hs.sudoku.feature.user.Authenticator
import javax.inject.Inject

@AndroidEntryPoint
class ProfileBottomSheetDialog : BottomSheetDialogFragment() {
    companion object {

        private const val EXTRA_USER_ID = "EXTRA_USER_ID"
        fun show(fragmentManager: FragmentManager, uid: String) =
            ProfileBottomSheetDialog()
                .apply { arguments = bundleOf(EXTRA_USER_ID to uid) }
                .show(fragmentManager, ProfileBottomSheetDialog::class.java.name)
    }

    private lateinit var binding: LayoutDialogUserProfileBinding
    private val profileViewModel: UserProfileViewModel by viewModels()
    private val behavior by lazy { (dialog as BottomSheetDialog).behavior }

    @Inject
    @UserModule.GoogleGamesAuthenticatorQualifier
    lateinit var authenticator: Authenticator

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
        val pagingDataAdapter = ProfileItemAdapter {
            when (it) {
                is ProfileItem.ChallengeLog ->
                    ChallengeItemBottomSheetDialog.show(childFragmentManager, it.item.challengeId)

                else -> {}
            }
        }.apply {
            addLoadStateListener { loadState ->
                when (loadState.refresh) {
                    is LoadState.NotLoading, is LoadState.Error -> dismissProgressIndicator()
                    LoadState.Loading -> showProgressIndicator()
                }
            }
        }

        with(binding.recyclerViewProfile) {
            layoutManager = LinearLayoutManager(context)
            addVerticalDivider(13.dp)
            adapter = pagingDataAdapter.withLoadStateFooter(PagingLoadStateAdapter())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            arguments?.getString(EXTRA_USER_ID)?.let { uid ->
                profileViewModel
                    .getProfilePagingData(authenticator, uid)
                    .observe(viewLifecycleOwner) { pagingDataAdapter.submitData(lifecycle, it) }
            }
        }
    }
}