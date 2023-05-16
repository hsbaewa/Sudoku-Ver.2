package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Fragment
import kr.co.hs.sudoku.databinding.LayoutPlayBattleForUserBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.views.CountDownView
import kr.co.hs.sudoku.views.SudokuBoardView

class BattlePlayFragment : Fragment() {
    companion object {
        fun new(uid: String): BattlePlayFragment {
            return BattlePlayFragment().apply {
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
    ) = with(LayoutPlayBattleForUserBinding.inflate(inflater, container, false)) {
        binding = this
        lifecycleOwner = this@BattlePlayFragment
        root
    }

    private lateinit var binding: LayoutPlayBattleForUserBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            battlePlayViewModel.getEventFlow(getUserId()!!).collect {
                when (it) {
                    is BattlePlayViewModel.Event.OnJoined -> it.setupUIForJoined()
                    is BattlePlayViewModel.Event.OnReady -> it.setupUIForReady()
                    is BattlePlayViewModel.Event.OnPrepared -> it.setupUIForPrepared()
                    is BattlePlayViewModel.Event.OnStarted -> it.setupUIForStart()
                    is BattlePlayViewModel.Event.OnChangedCellToCorrect -> it.setupUIForCellCorrect()
                    is BattlePlayViewModel.Event.OnChangedCellToError -> it.setupUIForCellError()
                    is BattlePlayViewModel.Event.OnChangedCell -> it.setupUIForChangedCell()
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
        binding.tvCountDown.setupUIPendingCountDown()
        binding.btnReadyOrStart.setupUIPending(participant.uid)
    }

    private fun BattlePlayViewModel.Event.OnReady.setupUIForReady() {
        binding.sudokuBoard.setupUIPending(battle.startingMatrix)
        binding.viewSilhouette.setupUIPendingSilhouette()
        binding.tvCountDown.setupUIPendingCountDown()
        when {
            isHost() -> binding.btnReadyOrStart.setupUIHost(participant.uid)
            isReady() -> binding.btnReadyOrStart.setupUIReady(participant.uid)
            else -> binding.btnReadyOrStart.setupUIPending(participant.uid)
        }
    }


    private fun BattlePlayViewModel.Event.OnPrepared.setupUIForPrepared() {
        binding.sudokuBoard.setupUIPending(battle.startingMatrix)
        binding.viewSilhouette.setupUIPendingSilhouette()
        binding.btnReadyOrStart.setupUIStarted()
        binding.tvCountDown.setupUIStartedCountDown {
            if (isHost()) {
                battlePlayViewModel.start(participant.uid)
            }
        }
    }


    private fun BattlePlayViewModel.Event.OnStarted.setupUIForStart() {
        binding.tvCountDown.setupUIStartedCountDown()
        binding.sudokuBoard.setupUIStarted(stage)
        binding.viewSilhouette.setupUIStartedSilhouette()
        binding.btnReadyOrStart.setupUIStarted()
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
        // 터치 동작 구현
        initListener()

        setRowCount(matrix.size, matrix)
    }

    private fun SudokuBoardView.initListener() {
        cellTouchDownListener = onCellTouchDown()
        cellValueChangedListener = onCellValueChangedListener()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardTouch 시점
     * @param
     * @return
     **/
    private fun onCellTouchDown() = { row: Int, column: Int ->
        val result = battlePlayViewModel.isMutableCell(row, column)
        result
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment SudokuBoardView 의 셀 값이 변경 된 경우 콜백
     **/
    private fun onCellValueChangedListener() = { row: Int, column: Int, value: Int? ->
        battlePlayViewModel[row, column] = value ?: 0; true
    }

    private fun SudokuBoardView.setupUIStarted(stage: Stage) {
        // 터치 동작 구현
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

    private fun CountDownView.setupUIPendingCountDown() {
        isVisible = true
        setAutoSizeText()
    }

    private inline fun CountDownView.setupUIStartedCountDown(crossinline onStart: () -> Unit) {
        setAutoSizeText()
        start(3) {
            isVisible = false
            onStart()
        }
    }

    private fun CountDownView.setupUIStartedCountDown() {
        setAutoSizeText()
        isVisible = false
    }

    private fun Button.setupUIHost(uid: String) {
        isVisible = true
        text = getString(R.string.caption_start_battle_for_host)
        setOnClickListener { battlePlayViewModel.pending(uid) }
    }

    private fun Button.setupUIPending(uid: String) {
        isVisible = true
        text = getString(R.string.caption_pending_battle_for_participant)
        setOnClickListener { battlePlayViewModel.ready(uid) }
    }

    private fun Button.setupUIReady(uid: String) {
        isVisible = true
        text = getString(R.string.caption_ready_battle_for_participant)
        setOnClickListener { battlePlayViewModel.releaseReady(uid) }
    }

    private fun Button.setupUIStarted() {
        isVisible = false
    }
}