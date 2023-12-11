package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutBattleDetailBinding
import kr.co.hs.sudoku.databinding.LayoutBattleLobbyBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.battle2.BattlePlayActivity
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.viewmodel.BattleLobbyViewModel
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel2

class BattleLobbyFragment : Fragment() {
    companion object {
        fun new() = BattleLobbyFragment()
    }

    private lateinit var binding: LayoutBattleLobbyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LayoutBattleLobbyBinding.inflate(inflater, container, false).also {
        binding = it
        it.lifecycleOwner = this
    }.root

    private val battlePlayViewModel: BattlePlayViewModel2 by viewModels {
        val app = requireContext().applicationContext as App
        BattlePlayViewModel2.ProviderFactory(app.getBattleRepository2())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- setup UI -----------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        with(binding) {
            swipeRefreshLayout.onCreatedRefreshLayout()
            recyclerView.onCreateBattleList()
            btnCreate.onCreatedNewButton()
        }


        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- observe LiveData -------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        battleLobbyViewModel.initObserver()


        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- request Data ---------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { initBattleLobby() }
        }

//        battlePlayViewModel.error.observe(viewLifecycleOwner) {
//            it.showError()
//        }
//        battlePlayViewModel.isRunningProgress.observe(viewLifecycleOwner) {
//            if (it) {
//                showProgressIndicator()
//            } else {
//                dismissProgressIndicator()
//            }
//        }
//        battlePlayViewModel.startParticipatingEventMonitoring()
//        battlePlayViewModel.battleEntity.observe(viewLifecycleOwner) {
//            if (it != null) {
//                startBattlePlayActivity(it.id)
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                showProgressIndicator()

                val participating = withContext(Dispatchers.IO) {
                    battlePlayViewModel
                        .runCatching { doGetParticipating() }
                        .getOrNull()
                        ?.takeIf { it is kr.co.hs.sudoku.model.battle2.BattleEntity.Opened || it is kr.co.hs.sudoku.model.battle2.BattleEntity.Playing || it is kr.co.hs.sudoku.model.battle2.BattleEntity.Pending }
                }

                participating?.run { startBattlePlayActivity(id) }


                dismissProgressIndicator()
            }


        }

    }

    // ViewModel for Lobby
    private val battleLobbyViewModel: BattleLobbyViewModel by viewModels()


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup UI -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun SwipeRefreshLayout.onCreatedRefreshLayout() {
        setOnRefreshListener { battleLobbyViewModel.loadPage(app.getBattleRepository()) }
    }

    private fun RecyclerView.onCreateBattleList() {
        layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        adapter = BattleLobbyListAdapter(onItemClick)
    }

    private val onItemClick = { item: BattleEntity ->
        battleLobbyViewModel.loadDetail(app.getBattleRepository(), item.id)
    }

    private fun Button.onCreatedNewButton() {
        setOnClickListener { startNewBattle() }
    }

    private fun startNewBattle() =
        viewLifecycleOwner.lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
            showSnackBar(throwable.message.toString())
        }) {
            currentUser
                ?.run { BattleCreateActivity.startBattleCreateActivity(requireContext()) }
                ?: throw Exception(getString(R.string.error_require_authenticate))
        }

    private val currentUser: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- observer for LiveData -------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun BattleLobbyViewModel.initObserver() {
        battleDetail.observe(viewLifecycleOwner) { it?.showDetail() }
//        battleCurrent.observe(viewLifecycleOwner) { it.startBattle() }
        battleList.observe(viewLifecycleOwner) { setupUIBattleList(it) }
        isRunningProgress.observe(viewLifecycleOwner) { setupUIProgress(it) }
        error.observe(viewLifecycleOwner) { it.showError() }
    }

    private fun BattleEntity.showDetail() {
        val dlgBinding = LayoutBattleDetailBinding.inflate(LayoutInflater.from(context))
        dlgBinding.sudokuBoard.setRowCount(startingMatrix.size, startingMatrix)
        dlgBinding.lifecycleOwner = this@BattleLobbyFragment

        val listAdapter = ParticipantListAdapter()
        val participants = participants.toList()
        listAdapter.submitList(participants)
        dlgBinding.participantsList.adapter = listAdapter

        val layoutManager = LinearLayoutManager(context)
        dlgBinding.participantsList.layoutManager = layoutManager
        dlgBinding.participantsList.setHasFixedSize(false)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dlgBinding.root)
            .setPositiveButton(R.string.join) { _, _ ->
//                takeIf {
//                    when (it) {
//                        is BattleEntity.PendingBattleEntity,
//                        is BattleEntity.RunningBattleEntity,
//                        is BattleEntity.WaitingBattleEntity -> true
//
//                        else -> false
//                    }
//                }?.startBattle()
                viewLifecycleOwner.lifecycleScope.launch {
                    showProgressIndicator()

                    withContext(Dispatchers.IO) {
                        battlePlayViewModel.doJoin(this@showDetail.id)
                    }

                    startBattlePlayActivity(this@showDetail.id)

                    dismissProgressIndicator()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setCancelable(false)
            .show()
    }

//    private fun BattleEntity.startBattle() =
//        currentUser?.let { startBattlePlayActivity(id) }

    private fun startBattlePlayActivity(battleId: String) =
        viewLifecycleOwner.lifecycleScope.launch {
//            activity.startBattlePlayActivity(uid, battleId)
//            BattlePlayActivity.start(requireContext(), battleId)
            startActivity(BattlePlayActivity.newIntent(requireContext(), battleId))
        }

    private fun Throwable.showError() = message.toString().run { showSnackBar(this) }

    private fun initBattleLobby() {
        val repo = app.getBattleRepository()
        currentUser
            ?.run { battleLobbyViewModel.requestBattleLobby(repo, this.uid) }
    }

    private fun setupUIBattleList(data: List<BattleEntity>) {
        with(binding) {
            (recyclerView.adapter as? BattleLobbyListAdapter)?.submitList(data)
            tvEmptyMessage.isVisible = data.isEmpty()
        }
    }

    private fun setupUIProgress(isShow: Boolean) {
        if (isShow) {
            if (!binding.swipeRefreshLayout.isRefreshing) {
                showProgressIndicator()
            }
        } else {
            with(binding.swipeRefreshLayout) {
                if (isRefreshing)
                    isRefreshing = false
            }
            dismissProgressIndicator()
        }
    }
}