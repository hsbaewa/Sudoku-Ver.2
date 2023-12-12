package kr.co.hs.sudoku.feature.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutPlayBattleForParticipantBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.views.SudokuBoardView

class ViewerBoardFragment : BoardFragment() {
    companion object {
        fun newInstance(matrix: IntMatrix) = ViewerBoardFragment()
            .apply { arguments = newInstanceArguments(matrix) }

        fun newInstanceArguments(matrix: IntMatrix) = bundleOf(
            EXTRA_FIX_CELL to matrix.flatten().toIntArray()
        )
    }

    private lateinit var binding: LayoutPlayBattleForParticipantBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutPlayBattleForParticipantBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override val board: SudokuBoardView
        get() = binding.sudokuBoard
    override val silhouette: View
        get() = binding.viewSilhouette
    private val statusMessage: TextView
        get() = binding.tvStatusMessage

    override fun onCellTouchDown(row: Int, column: Int) = false
    override fun onCellValueChangedListener(row: Int, column: Int, value: Int?): Boolean = false

    private var message: CharSequence?
        set(value) {
            value
                ?.run {
                    statusMessage.text = this
                    statusMessage.setAutoSizeText()
                    statusMessage.isVisible = true
                }
                ?: run {
                    statusMessage.text = null
                    statusMessage.isVisible = false
                }
        }
        get() = statusMessage.text.toString()

    override fun setStatus(participant: ParticipantEntity) {
        when (participant) {
            is ParticipantEntity.Cleared -> {
                setStatus(false, "clear")
            }

            is ParticipantEntity.Guest -> setStatus(
                false,
                getString(R.string.status_pending_battle_for_participant)
            )

            is ParticipantEntity.Host -> setStatus(
                false,
                getString(R.string.status_host_battle_for_participant)
            )

            is ParticipantEntity.Playing -> {
                setStatus(true, null)
                setValues(participant.matrix)
            }

            is ParticipantEntity.ReadyGuest -> setStatus(
                false,
                getString(R.string.status_ready_battle_for_participant)
            )
        }
    }

    fun setStatus(enabled: Boolean, message: CharSequence?) {
        this.enabled = enabled
        this.message = message
    }
}