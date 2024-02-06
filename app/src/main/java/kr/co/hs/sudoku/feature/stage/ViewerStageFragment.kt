package kr.co.hs.sudoku.feature.stage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import kr.co.hs.sudoku.databinding.LayoutPlayBattleForParticipantBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.views.SudokuView

open class ViewerStageFragment : StageFragment() {

    override val board: SudokuView
        get() = binding.sudokuView
    override val silhouette: View
        get() = binding.viewSilhouette
    private val statusMessage: TextView
        get() = binding.tvStatusMessage

    override fun onCellTouchDown(row: Int, column: Int) = false
    override fun onCellValueChangedListener(row: Int, column: Int, value: Int?): Boolean = false


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

    fun setStatus(enabled: Boolean, message: CharSequence?) {
        this.enabled = enabled
        this.message = message
    }
}