package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayBattleForParticipantBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.views.SudokuBoardView

class BattleParticipantFragment : Fragment() {
    companion object {
        fun new(uid: String): BattleParticipantFragment {
            return BattleParticipantFragment().apply {
                arguments = Bundle().apply {
                    putUserId(uid)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = with(LayoutPlayBattleForParticipantBinding.inflate(inflater, container, false)) {
        binding = this
        lifecycleOwner = this@BattleParticipantFragment
        root
    }

    private lateinit var binding: LayoutPlayBattleForParticipantBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            battlePlayViewModel.getEventFlow(getUserId()!!).collect {
                when (it) {
                    is BattlePlayViewModel.Event.OnJoined -> it.setupUIForJoined()
                    is BattlePlayViewModel.Event.OnReady -> it.setupUIForReady()
                    is BattlePlayViewModel.Event.OnPrepared -> it.setupUIForPrepared()
                    is BattlePlayViewModel.Event.OnStarted -> it.setupUIForStart()
                    is BattlePlayViewModel.Event.OnChangedCell -> it.setupUIForChangedCell()
                    is BattlePlayViewModel.Event.OnChangedCellToCorrect -> it.setupUIForCellCorrect()
                    is BattlePlayViewModel.Event.OnChangedCellToError -> it.setupUIForCellError()
                    else -> {}
                }
            }
        }
    }

    // play View Model
    private val battlePlayViewModel: BattlePlayViewModel by activityViewModels()

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Event -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun BattlePlayViewModel.Event.OnJoined.setupUIForJoined() {
        binding.sudokuBoard.setupUIPending(battle.startingMatrix)
        binding.viewSilhouette.setupUIPendingSilhouette()
        when {
            isHost() -> binding.tvStatusMessage.setupUIStatusHost()
            isReady() -> binding.tvStatusMessage.setupUIStatusReady()
            else -> binding.tvStatusMessage.setupUIStatusPending()
        }
    }

    private fun BattlePlayViewModel.Event.OnReady.setupUIForReady() {
        binding.sudokuBoard.setupUIPending(battle.startingMatrix)
        binding.viewSilhouette.setupUIPendingSilhouette()
        when {
            isHost() -> binding.tvStatusMessage.setupUIStatusHost()
            isReady() -> binding.tvStatusMessage.setupUIStatusReady()
            else -> binding.tvStatusMessage.setupUIStatusPending()
        }
    }

    private fun BattlePlayViewModel.Event.OnPrepared.setupUIForPrepared() {
        binding.sudokuBoard.setupUIPending(battle.startingMatrix)
        binding.viewSilhouette.setupUIPendingSilhouette()
        binding.tvStatusMessage.setupUIStatusStarted()
    }

    private fun BattlePlayViewModel.Event.OnStarted.setupUIForStart() {
        binding.sudokuBoard.setupUIStarted(stage)
        binding.viewSilhouette.setupUIStartedSilhouette()
        binding.tvStatusMessage.setupUIStatusStarted()
    }

    private fun BattlePlayViewModel.Event.OnChangedCellToCorrect.setupUIForCellCorrect() {
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, false) }
    }

    private fun BattlePlayViewModel.Event.OnChangedCellToError.setupUIForCellError() {
        set.mapNotNull { (it as? IntCoordinateCellEntity)?.run { Pair(row, column) } }
            .forEach { binding.sudokuBoard.setError(it.first, it.second, true) }
    }

    private fun BattlePlayViewModel.Event.OnChangedCell.setupUIForChangedCell() {
        binding.sudokuBoard.setCellValue(row, column, value ?: 0)
    }

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Event Extension --------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun BattlePlayViewModel.Event.isHost() = battle.host == participant.uid
    private fun BattlePlayViewModel.Event.isReady() = participant.isReady


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- setup UI -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    private fun SudokuBoardView.setupUIPending(matrix: List<List<Int>>) {
        // 터치 동작 안함
        initListener()

        setRowCount(matrix.size, matrix)
    }

    private fun SudokuBoardView.initListener() {
        cellTouchDownListener = { _: Int, _: Int -> false }
        cellValueChangedListener = { _: Int, _: Int, _: Int? -> false }
    }

    private fun SudokuBoardView.setupUIStarted(stage: Stage) {
        // 터치 동작 안함
        initListener()

        (0 until stage.rowCount).forEach { row ->
            (0 until stage.columnCount).forEach { column ->
                runCatching { stage[row, column] }.getOrNull()
                    ?.takeIf { it > 0 }
                    ?.run { setCellValue(row, column, this) }
            }
        }
    }

    private fun View.setupUIPendingSilhouette() {
        isVisible = true
    }

    private fun View.setupUIStartedSilhouette() {
        isVisible = false
    }

    private fun TextView.setupUIStatusHost() {
        isVisible = true
        text = getString(R.string.status_host_battle_for_participant)
        setAutoSizeText()
    }

    private fun TextView.setupUIStatusPending() {
        isVisible = true
        text = getString(R.string.status_pending_battle_for_participant)
        setAutoSizeText()
    }

    private fun TextView.setupUIStatusReady() {
        isVisible = true
        text = getString(R.string.status_ready_battle_for_participant)
        setAutoSizeText()
    }

    private fun TextView.setupUIStatusStarted() {
        isVisible = false
    }
}