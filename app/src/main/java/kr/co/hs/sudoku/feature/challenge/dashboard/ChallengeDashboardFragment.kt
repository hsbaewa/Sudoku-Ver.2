package kr.co.hs.sudoku.feature.challenge.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutListChallengeRankBinding
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.profile.ProfileBottomSheetDialog
import kr.co.hs.sudoku.feature.ad.ChallengeRetryRewardAdManager
import kr.co.hs.sudoku.feature.challenge.play.ChallengePlayActivity
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import java.util.Calendar
import java.util.Date

class ChallengeDashboardFragment : Fragment() {
    companion object {
        fun newInstance() = ChallengeDashboardFragment()
    }

    private lateinit var binding: LayoutListChallengeRankBinding
    private val viewModel: ChallengeDashboardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutListChallengeRankBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.recyclerViewRankList) {
            layoutManager = LinearLayoutManager(context)
            addVerticalDivider(thickness = 10.dp)
            adapter = ChallengeDashboardListItemAdapter(
                onClickStart = { startChallenge(it) },
                onClickSelectDate = { showChallengeSelectDialog() },
                onClickShowProfile = { showUserProfile(it) }
            )
        }

        with(binding.recyclerViewRankList.adapter as ChallengeDashboardListItemAdapter) {
            submitList(listOf(ChallengeDashboardListItem.TitleItem(null)))
        }

        viewModel.dashboardItemList.observe(viewLifecycleOwner) {
            with(binding.recyclerViewRankList.adapter as ChallengeDashboardListItemAdapter) {
                submitList(it)
            }
        }


        with(binding.swipeRefreshLayout) {
            setOnRefreshListener { viewModel.refreshChallenge() }
            viewModel.isRunningProgress.observe(viewLifecycleOwner) {
                if (isRefreshing) {
                    if (!it) {
                        binding.swipeRefreshLayout.isRefreshing = it
                    }
                } else {
                    isShowProgressIndicator = it
                }

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.refreshChallenge() }
        }

    }

    private fun startChallenge(challengeEntity: ChallengeEntity) =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            when (throwable) {
                is AlreadyException -> showConfirm(
                    getString(R.string.app_name),
                    throwable.message.toString()
                ) {
                    if (it) {
                        retryChallenge(challengeEntity)
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
            ChallengePlayActivity.start(requireContext(), challengeEntity.challengeId)
        }

    private class AlreadyException(message: String?) : Exception(message)


    private fun retryChallenge(challengeEntity: ChallengeEntity) {
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            dismissProgressIndicator()
            showAlert(getString(R.string.app_name), throwable.message.toString()) {}
        }) {
            showProgressIndicator()
            val result = ChallengeRetryRewardAdManager(requireActivity()).showRewardedAd()
            if (!result)
                throw InvalidRewardedException(getString(R.string.error_challenge_retry_rewarded))

            viewModel.doDeleteRecord()
            dismissProgressIndicator()

            ChallengePlayActivity.start(requireContext(), challengeEntity.challengeId)
        }
    }

    private class InvalidRewardedException(message: String?) : Exception(message)

    private fun showChallengeSelectDialog() = viewLifecycleOwner.lifecycleScope.launch {
        val dialog = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                viewModel.challengeList.value
                    ?.find {
                        val c = it.createdAt?.let {
                            Calendar.getInstance().apply { time = it }
                        } ?: return@find false

                        val y = c.get(Calendar.YEAR)
                        val m = c.get(Calendar.MONTH)
                        val d = c.get(Calendar.DAY_OF_MONTH)

                        y == year && m == monthOfYear && d == dayOfMonth
                    }
                    ?.let { viewModel.selectChallenge(it) }
            },
            Calendar.getInstance()
                .apply { time = viewModel.selected.value?.createdAt ?: Date() }
        )
        dialog.selectableDays = viewModel.challengeList.value
            ?.mapNotNull {
                it.createdAt?.let { c -> Calendar.getInstance().apply { time = c } }
            }
            ?.toTypedArray()

        dialog.vibrate(false)
        dialog.show(childFragmentManager, "DatePickerDialog")
    }

    private fun showUserProfile(uid: String): Boolean {
        lifecycleScope.launch { ProfileBottomSheetDialog.show(childFragmentManager, uid) }
        return true
    }
}