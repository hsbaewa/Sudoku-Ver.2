package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutPlayBattleForUserBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.viewmodel.BattlePlayViewModel
import kr.co.hs.sudoku.views.CountDownView
import kr.co.hs.sudoku.views.SudokuBoardView

class ControlBoardFragment : BoardFragment() {
    companion object {
        fun newInstance(matrix: IntMatrix) = ControlBoardFragment()
            .apply { arguments = newInstanceArguments(matrix) }

        fun newInstanceArguments(matrix: IntMatrix) = bundleOf(
            EXTRA_FIX_CELL to matrix.flatten().toIntArray()
        )
    }

    private val viewModel: BattlePlayViewModel by activityViewModels()
    private lateinit var binding: LayoutPlayBattleForUserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutPlayBattleForUserBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override val board: SudokuBoardView
        get() = binding.sudokuBoard
    override val silhouette: View
        get() = binding.viewSilhouette
    private val countDownView: CountDownView
        get() = binding.tvCountDown
    private val btn: MaterialButton
        get() = binding.btnReadyOrStart

    override fun onCellTouchDown(row: Int, column: Int) = true
    override fun onCellValueChangedListener(row: Int, column: Int, value: Int?): Boolean {
        setValue(row, column, value ?: 0)
        viewLifecycleOwner.lifecycleScope
            .launch(CoroutineExceptionHandler { _, _ -> })
            { viewModel.doUpdateMatrix(row, column, value ?: 0) }
        return true
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn.setOnClickListener { viewModel.toggleReadyOrStart() }
    }


    private var btnText: CharSequence?
        set(value) {
            value
                ?.run {
                    btn.also {
                        it.text = this
                        it.isVisible = true
                    }
                }
                ?: run {
                    btn.also {
                        it.text = null
                        it.isVisible = false
                    }
                }
        }
        get() = btn.text.toString()

    override fun setStatus(participant: ParticipantEntity) {
        when (participant) {
            is ParticipantEntity.Cleared -> setStatus(false, null)
            is ParticipantEntity.Guest -> setStatus(
                false,
                getString(R.string.caption_pending_battle_for_participant)
            )

            is ParticipantEntity.Host -> setStatus(
                false,
                getString(R.string.caption_start_battle_for_host)
            )

            is ParticipantEntity.Playing -> {
                setStatus(true, null)
                setValues(participant.matrix)
            }

            is ParticipantEntity.ReadyGuest -> setStatus(
                false,
                getString(R.string.caption_ready_battle_for_participant)
            )
        }
    }

    fun setStatus(enabled: Boolean, buttonCaption: CharSequence?) {
        this.enabled = enabled
        this.btnText = buttonCaption
    }

    fun startCountDown(onAfter: () -> Unit) = with(countDownView) {
        setAutoSizeText()
        start(3) {
            isVisible = false
            onAfter()
        }
    }

}