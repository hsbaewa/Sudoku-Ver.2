package kr.co.hs.sudoku.feature

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityGuideBinding
import kr.co.hs.sudoku.di.repositories.RegistrationRepositoryQualifier
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.repository.settings.RegistrationRepository
import kr.co.hs.sudoku.usecase.BuildSudokuUseCaseImpl
import kr.co.hs.sudoku.views.SudokuView
import javax.inject.Inject


@AndroidEntryPoint
class GuideActivity : Activity() {

    companion object {
        private fun newIntent(context: Context) = Intent(context, GuideActivity::class.java)
        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityGuideBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_guide) }

    @Inject
    @RegistrationRepositoryQualifier
    lateinit var registrationRepository: RegistrationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        showStep1()

        onBackPressedDispatcher.addCallback {
            showConfirm(R.string.app_name, R.string.sudoku_guide_exit_alert) {
                if (it) {
                    lifecycleScope.launch {
                        showProgressIndicator()
                        withContext(Dispatchers.IO) { registrationRepository.seenTutorial() }
                        dismissProgressIndicator()
                        finish()
                    }
                }
            }
        }
    }

    private fun showStep1() = lifecycleScope.launch {
        with(binding.sudokuView) {
            setFixedCellValues(4)
            isVisibleNumber = false
            isClickable = false
        }

        binding.tvDescription.setText(R.string.sudoku_guide_desc_1)

        with(binding.btnNext) {
            setText(R.string.next)
            setOnClickListener { showStep2() }
        }
    }

    private fun showStep2() {
        val job = Job()
        lifecycleScope.launch(job) {
            while (true) {
                with(binding.sudokuView) {
                    isVisibleRowGuide = true
                    isVisibleColumGuide = false
                    isVisibleBoxGuide = false

                    showGuide(0, 0)
                    delay(500)
                    showGuide(1, 0)
                    delay(500)
                    showGuide(2, 0)
                    delay(500)
                    showGuide(3, 0)
                    delay(500)
                }

                with(binding.sudokuView) {
                    isVisibleRowGuide = false
                    isVisibleColumGuide = true
                    isVisibleBoxGuide = false

                    showGuide(0, 0)
                    delay(500)
                    showGuide(0, 1)
                    delay(500)
                    showGuide(0, 2)
                    delay(500)
                    showGuide(0, 3)
                    delay(500)
                }

                with(binding.sudokuView) {
                    isVisibleRowGuide = false
                    isVisibleColumGuide = false
                    isVisibleBoxGuide = true

                    showGuide(1, 1)
                    delay(500)
                    showGuide(1, 2)
                    delay(500)
                    showGuide(2, 1)
                    delay(500)
                    showGuide(2, 2)
                    delay(500)
                }
            }
        }.invokeOnCompletion { binding.sudokuView.dismissGuide() }

        lifecycleScope.launch {
            with(binding.sudokuView) {
                setFixedCellValues(4)
                isVisibleNumber = false
                isClickable = false
            }

            binding.tvDescription.setText(R.string.sudoku_guide_desc_2)
            with(binding.btnNext) {
                binding.btnNext.setText(R.string.next)
                setOnClickListener {
                    job.cancel()
                    showStep3()
                }
            }
        }
    }

    private fun showStep3() = lifecycleScope.launch {
        with(binding.sudokuView) {
            setFixedCellValues(
                listOf(
                    listOf(1, 2, 3, 4),
                    listOf(3, 0, 1, 2),
                    listOf(2, 3, 4, 1),
                    listOf(4, 1, 2, 3)
                )
            )
            isVisibleNumber = true
            isClickable = false
        }

        binding.tvDescription.setText(R.string.sudoku_guide_desc_3)
        with(binding.btnNext) {
            binding.btnNext.setText(R.string.next)
            setOnClickListener { showStep4() }
        }
    }

    private fun showStep4() = lifecycleScope.launch {
        with(binding.sudokuView) {
            setFixedCellValues(
                listOf(
                    listOf(1, 2, 3, 4),
                    listOf(3, 0, 1, 2),
                    listOf(2, 3, 4, 1),
                    listOf(4, 1, 2, 3)
                )
            )
            isVisibleNumber = true
            isClickable = false

            isVisibleBoxGuide = false
            isVisibleRowGuide = false
            isVisibleColumGuide = false
            showGuide(1, 1)
            showNumberSelection(1, 1)
        }

        binding.tvDescription.setText(R.string.sudoku_guide_desc_4)
        with(binding.btnNext) {
            binding.btnNext.setText(R.string.next)
            setOnClickListener {
                with(binding.sudokuView) {
                    dismissNumberSelection()
                    dismissGuide()
                }
                showStep5()
            }
        }
    }

    private fun showStep5() {
        val job = Job()
        lifecycleScope.launch(job) {
            while (true) {
                (1..4).forEach { number ->
                    with(binding.sudokuView) {
                        isVisibleBoxGuide = false
                        isVisibleRowGuide = false
                        isVisibleColumGuide = false
                        showGuide(1, 1)
                    }
                    delay(500)

                    with(binding.sudokuView) {
                        isVisibleBoxGuide = true
                        isVisibleRowGuide = true
                        isVisibleColumGuide = true
                        showGuide(1, 1)
                        showNumberSelection(1, 1)
                    }
                    delay(500)

                    with(binding.sudokuView) {
                        isVisibleBoxGuide = true
                        isVisibleRowGuide = true
                        isVisibleColumGuide = true
                        showGuide(1, 1)
                        setTouchSelectNumber(number)
                    }
                    delay(500)

                    with(binding.sudokuView) {
                        dismissGuide()
                        setTouchUpSelectNumber()
                        setCellValue(1, 1, number)
                    }
                    delay(500)
                }
            }
        }.invokeOnCompletion {
            with(binding.sudokuView) {
                clearCellValues()
                dismissGuide()
                dismissNumberSelection()
            }
        }

        lifecycleScope.launch {
            with(binding.sudokuView) {
                setFixedCellValues(
                    listOf(
                        listOf(1, 2, 3, 4),
                        listOf(3, 0, 1, 2),
                        listOf(2, 3, 4, 1),
                        listOf(4, 1, 2, 3)
                    )
                )
                isVisibleNumber = true
                isClickable = false
            }

            binding.tvDescription.setText(R.string.sudoku_guide_desc_5)
            with(binding.btnNext) {
                setText(R.string.next)
                setOnClickListener {
                    job.cancel()
                    showStep6()
                }
            }
        }
    }

    private fun showStep6() = lifecycleScope.launch {
        val matrix = CustomMatrix(
            listOf(
                listOf(1, 2, 3, 4),
                listOf(3, 0, 1, 2),
                listOf(2, 3, 4, 1),
                listOf(4, 1, 2, 3)
            )
        )
        val stage = BuildSudokuUseCaseImpl(matrix).invoke().last()
        with(binding.sudokuView) {
            setFixedCellValues(stage.toValueTable())
            isVisibleNumber = true
            isClickable = true
            setOnCellValueChangedListener(object : SudokuView.CellValueChangedListener {
                override fun onChangedCell(row: Int, column: Int, value: Int?) {
                    stage[row, column] = value ?: 0
                    val errorValues =
                        List(stage.rowCount) { MutableList(stage.columnCount) { false } }
                    val currentError = stage.getDuplicatedCells().toList().toSet()
                    currentError.forEach {
                        with(it as IntCoordinateCellEntity) {
                            errorValues[this.row][this.column] = true
                        }
                    }
                    setError(errorValues)

                    if (stage.isSudokuClear()) {
                        binding.sudokuView.isClickable = false
                        binding.tvDescription.text = getString(R.string.sudoku_guide_desc_7)
                        binding.btnNext.isVisible = true
                    }
                }
            })
        }
        binding.tvDescription.text = getString(R.string.sudoku_guide_desc_6)

        with(binding.btnNext) {
            isVisible = false
            setText(R.string.sudoku_guide_finish)
            setOnClickListener {
                lifecycleScope.launch {
                    showProgressIndicator()
                    withContext(Dispatchers.IO) { registrationRepository.seenTutorial() }
                    dismissProgressIndicator()
                    finish()
                }
            }
        }
    }
}