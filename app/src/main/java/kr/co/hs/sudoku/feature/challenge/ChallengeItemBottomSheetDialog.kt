package kr.co.hs.sudoku.feature.challenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.core.BottomSheetDialogFragment
import kr.co.hs.sudoku.databinding.LayoutDialogChallengeItemBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.ad.ChallengeRetryRewardAdManager
import kr.co.hs.sudoku.feature.challenge.play.ChallengePlayActivity
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
    private val launcherForChallengePlay =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == android.app.Activity.RESULT_OK) {
                val challengeId = it.data?.getStringExtra(ChallengePlayActivity.EXTRA_CHALLENGE_ID)
                    ?: return@registerForActivityResult
                updateChallengeInfo(challengeId)
            }
        }

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
        val challengeId = arguments?.getString(EXTRA_CHALLENGE_ID)
            ?: throw Exception("invalid challenge id")

        with(binding.challengeItemView) {
            setOnProfileClickListener(this@ChallengeItemBottomSheetDialog)
            setOnClickShowLeaderBoard { showLeaderBoard(challengeId) }
        }

        updateChallengeInfo(challengeId)
    }

    private fun updateChallengeInfo(challengeId: String) {
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, _ ->
            dismissProgressIndicator()
        }) {
            showProgressIndicator()
            val challengeEntity: ChallengeEntity
            val leaderBoardList: List<RankerEntity>
            with(app.getChallengeRepository()) {
                challengeEntity = withContext(Dispatchers.IO) { getChallenge(challengeId) }
                leaderBoardList = withContext(Dispatchers.IO) { getRecords(challengeId) }
            }
            dismissProgressIndicator()

            with(binding.challengeItemView) {
                set(challengeEntity)
                setLeaderBoard(leaderBoardList)
                isVisible = true
            }

            with(binding.btnStart) {
                setOnClickListener { startChallenge(challengeEntity) }
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


    private fun startChallenge(challengeEntity: ChallengeEntity) =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is AlreadyException -> showConfirm(
                    getString(R.string.app_name),
                    throwable.message.toString()
                ) {
                    if (it) {
                        retryChallenge(challengeEntity.challengeId)
                    }
                }

                else -> showAlert(
                    getString(R.string.app_name),
                    throwable.message.toString()
                ) {}
            }
        }) {
            if (challengeEntity.isComplete)
                throw AlreadyException(getString(R.string.error_challenge_already_record))
            launcherForChallengePlay.launch(
                ChallengePlayActivity.newIntent(
                    requireContext(),
                    challengeEntity.challengeId
                )
            )
        }

    private class AlreadyException(message: String?) : Exception(message)


    private fun retryChallenge(challengeId: String) {
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showAlert(getString(R.string.app_name), throwable.message.toString()) {}
        }) {
            showProgressIndicator()
            val result = ChallengeRetryRewardAdManager(requireActivity()).showRewardedAd()
            if (!result)
                throw InvalidRewardedException(getString(R.string.error_challenge_retry_rewarded))

            val app = requireContext().applicationContext as App

            FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserUid ->
                withContext(Dispatchers.IO) {
                    app.getChallengeRepository().deleteRecord(challengeId, currentUserUid)
                }

                dismissProgressIndicator()
                ChallengePlayActivity.start(requireContext(), challengeId)
            } ?: run {
                throw Exception("invalid uid")
            }
        }
    }

    private class InvalidRewardedException(message: String?) : Exception(message)
}