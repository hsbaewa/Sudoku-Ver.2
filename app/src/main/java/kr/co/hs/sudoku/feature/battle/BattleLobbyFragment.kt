package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutBattleDetailBinding
import kr.co.hs.sudoku.databinding.LayoutBattleLobbyBinding
import kr.co.hs.sudoku.extension.platform.FragmentExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.FragmentExtension.showSnackBar
import kr.co.hs.sudoku.feature.battle.BattleCreateActivity.Companion.startBattleCreateActivity
import kr.co.hs.sudoku.feature.battle.BattlePlayActivity.Companion.startBattlePlayActivity
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.viewmodel.BattleLobbyViewModel

class BattleLobbyFragment : Fragment() {
    companion object {
        fun new(uid: String?) = BattleLobbyFragment().apply {
            arguments = Bundle().apply {
                uid?.run { putUserId(this) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = LayoutBattleLobbyBinding.inflate(inflater, container, false).also {
        binding = it
        it.lifecycleOwner = this
    }.root

    private lateinit var binding: LayoutBattleLobbyBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- setup UI -----------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        binding.swipeRefreshLayout.setupUIRefreshLayout(battleLobbyViewModel.isRunningProgress)
        binding.recyclerView.setupUIBattleList(battleLobbyViewModel.battleList)
        binding.tvEmptyMessage.setupUIEmptyMessage(battleLobbyViewModel.battleList)
        binding.btnCreate.setupUIForCreateButton(getUserId())


        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- observe LiveData -------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        battleLobbyViewModel.run {
            error.observe(viewLifecycleOwner, observeError)
            isRunningProgress.observe(viewLifecycleOwner, observeProgress)
            battleDetail.observe(viewLifecycleOwner, observeDetail)
            battleCurrent.observe(viewLifecycleOwner, observeCurrent)
        }


        //--------------------------------------------------------------------------------------------\\
        //----------------------------------------- request Data ---------------------------------------\\
        //--------------------------------------------------------------------------------------------\\
        viewLifecycleOwner.lifecycleScope.launch {
            withStarted { battleLobbyViewModel.loadPage(app.getBattleRepository()) }
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getUserId()
                    ?.run {
                        battleLobbyViewModel.loadCurrent(app.getBattleRepository(), this)
                    }
                    ?: kotlin.run {
                        showSnackBar(getString(R.string.error_require_authenticate))
                    }
            }
        }
    }

    // ViewModel for Lobby
    private val battleLobbyViewModel: BattleLobbyViewModel by activityViewModels()


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup UI -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun SwipeRefreshLayout.setupUIRefreshLayout(progress: LiveData<Boolean>) {
        setOnRefreshListener { battleLobbyViewModel.loadPage(app.getBattleRepository()) }
        progress.observe(viewLifecycleOwner) {
            it.takeUnless { it }?.run {
                isRefreshing = false
            }
        }
    }

    private fun RecyclerView.setupUIBattleList(data: LiveData<List<BattleEntity>>) {
        layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        adapter = BattleLobbyListAdapter { _, item ->
            battleLobbyViewModel.loadDetail(app.getBattleRepository(), item.id)
        }.apply {
            data.observe(viewLifecycleOwner) {
                this.submitList(it)
            }
        }
    }

    private fun TextView.setupUIEmptyMessage(data: LiveData<List<BattleEntity>>) {
        data.observe(viewLifecycleOwner) {
            it.takeIf { it.isEmpty() }
                ?.run {
                    visibility = View.VISIBLE
                }
                ?: kotlin.run {
                    visibility = View.GONE
                }
        }
    }


    private fun Button.setupUIForCreateButton(uid: String?) {
        setOnClickListener {
            uid.takeIf { it != null }
                ?.run {
                    activity.startBattleCreateActivity(this)
                }
                ?: kotlin.run {
                    showSnackBar(getString(R.string.error_require_authenticate))
                }
        }
    }


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- observer for LiveData -------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private val observeError = Observer<Throwable> {
        it.message?.run { showSnackBar(this) }
    }

    private val observeProgress = Observer<Boolean> {
        it.takeIf { it }?.run { showProgressIndicator() } ?: dismissProgressIndicator()
    }

    private val observeCurrent = Observer<BattleEntity?> { battle ->
        getUserId()?.let { uid ->
            if (battle != null) {
                activity.startBattlePlayActivity(uid, battle.id)
            }
        }
    }

    private val observeDetail = Observer<BattleEntity> {
        it.run { showDetail(this) }
    }


    private fun showDetail(battle: BattleEntity) {
        val dlgBinding = LayoutBattleDetailBinding.inflate(LayoutInflater.from(context))
        dlgBinding.sudokuBoard.setRowCount(battle.startingMatrix.size, battle.startingMatrix)
        dlgBinding.lifecycleOwner = this

        val listAdapter = ParticipantListAdapter()
        val participants = battle.participants.toList()
        listAdapter.submitList(participants)
        dlgBinding.participantsList.adapter = listAdapter

        val layoutManager = LinearLayoutManager(context)
        dlgBinding.participantsList.layoutManager = layoutManager
        dlgBinding.participantsList.setHasFixedSize(false)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dlgBinding.root)
            .setPositiveButton(R.string.join) { _, _ ->
                getUserId()?.let { uid ->
                    lifecycleScope.launch {
                        activity.startBattlePlayActivity(uid, battle.id)
                    }
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setCancelable(false)
            .show()
    }
}