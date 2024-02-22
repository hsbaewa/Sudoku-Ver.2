package kr.co.hs.sudoku.feature.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.LayoutDialogChallengeItemBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.leaderboard.LeaderBoardBottomSheetDialogFragment
import kr.co.hs.sudoku.feature.multi.MultiPlayCreateActivity
import kr.co.hs.sudoku.feature.profile.ProfileBottomSheetDialog
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity

class ChallengeItemBottomSheetDialog : BottomSheetDialogFragment(),
    ProfilePopupMenu.OnPopupMenuItemClickListener {

    companion object {
        private const val EXTRA_CHALLENGE_ID = "EXTRA_CHALLENGE_ID"
        fun show(fragmentManager: FragmentManager, challengeId: String) =
            ChallengeItemBottomSheetDialog()
                .apply { arguments = bundleOf(EXTRA_CHALLENGE_ID to challengeId) }
                .show(fragmentManager, ChallengeItemBottomSheetDialog::class.java.name)
    }

    private lateinit var binding: LayoutDialogChallengeItemBinding
    private val app: App by lazy { requireContext().applicationContext as App }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogChallengeItemBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.challengeItemView) {
            setOnProfileClickListener(this@ChallengeItemBottomSheetDialog)
        }

        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, _ ->
            dismissProgressIndicator()
        }) {
            showProgressIndicator()
            val challengeId =
                arguments?.getString(EXTRA_CHALLENGE_ID) ?: throw Exception("invalid challenge id")
            val challengeEntity: ChallengeEntity
            val leaderBoardList: List<RankerEntity>
            with(app.getChallengeRepository()) {
                challengeEntity = withContext(Dispatchers.IO) { getChallenge(challengeId) }
                leaderBoardList = withContext(Dispatchers.IO) { getRecords(challengeId) }
            }
            dismissProgressIndicator()

            with(binding.challengeItemView) {
                isVisible = true
                set(challengeEntity)
                setLeaderBoard(leaderBoardList)
                setOnClickShowLeaderBoard { showLeaderBoard(challengeId) }
            }
        }
    }

    override fun onClickProfile(uid: String) =
        ProfileBottomSheetDialog.show(childFragmentManager, uid)

    override fun onClickInviteMultiPlay(uid: String, displayName: String) {
        val title = getString(R.string.multi_play_invite_confirm_title)
        val message = getString(R.string.multi_play_invite_confirm_message, displayName)
        (requireActivity() as Activity).showConfirm(title, message) {
            if (it) {
                dismiss()
                MultiPlayCreateActivity.start(requireActivity(), uid)
            }
        }
    }

    private fun showLeaderBoard(challengeId: String) = viewLifecycleOwner.lifecycleScope.launch {
        LeaderBoardBottomSheetDialogFragment
            .showChallengeLeaderBoard(childFragmentManager, challengeId)
    }
}