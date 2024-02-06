package kr.co.hs.sudoku.feature.stage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import kr.co.hs.sudoku.databinding.LayoutPlayBattleForUserBinding
import kr.co.hs.sudoku.extension.platform.TextViewExtension.setAutoSizeText
import kr.co.hs.sudoku.views.CountDownView
import kr.co.hs.sudoku.views.SudokuView

open class ControlStageFragment : StageFragment() {
    override val board: SudokuView
        get() = binding.sudokuView
    override val silhouette: View
        get() = binding.viewSilhouette
    private val countDownView: CountDownView
        get() = binding.tvCountDown
    private val btn: MaterialButton
        get() = binding.btnReadyOrStart

    override fun onCellTouchDown(row: Int, column: Int) = true
    override fun onCellValueChangedListener(row: Int, column: Int, value: Int?): Boolean {
        setValue(row, column, value ?: 0)
        return true
    }

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn.setOnClickListener { onClickFunctionButton() }
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

    open fun onClickFunctionButton() {}
}