package kr.co.hs.sudoku.feature.aiplay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityPlayMultiBinding
import kr.co.hs.sudoku.databinding.LayoutCompleteBinding
import kr.co.hs.sudoku.extension.CoilExt.load
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat
import kr.co.hs.sudoku.feature.battle.MultiPlayControlStageFragment
import kr.co.hs.sudoku.feature.battle.MultiPlayViewerStageFragment
import kr.co.hs.sudoku.feature.stage.StageFragment
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.EmptyMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.model.stage.IntCoordinateCellEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.parcel.MatrixParcelModel
import kr.co.hs.sudoku.viewmodel.RecordViewModel

class AIGameActivity : Activity(), IntCoordinateCellEntity.ValueChangedListener {
    companion object {
        private const val EXTRA_MATRIX = "EXTRA_MATRIX"
        fun newIntent(context: Context, matrix: IntMatrix) =
            Intent(context, AIGameActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_MATRIX, MatrixParcelModel(matrix))

        fun start(context: Context, matrix: IntMatrix) =
            context.startActivity(newIntent(context, matrix))
    }

    private val startingMatrix: IntMatrix by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_MATRIX, MatrixParcelModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_MATRIX)
        }?.matrix
            ?.run { CustomMatrix(this) }
            ?: EmptyMatrix()
    }
    private val binding: ActivityPlayMultiBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_play_multi) }
    private val recordViewModel: RecordViewModel by viewModels()
    private var lastKnownUserProfile: ProfileEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        onBackPressedDispatcher.addCallback { showExitDialog() }

        recordViewModel.timer.observe(this) { binding.tvTimer.text = it }

        controlBoard {
//            it.setval
        }
    }

    fun controlBoard(on: (MultiPlayControlStageFragment) -> Unit) = with(supportFragmentManager) {
        val tag = MultiPlayControlStageFragment::class.java.simpleName
        findFragmentByTag(tag)?.run { this as? MultiPlayControlStageFragment }
            ?.also(on)
            ?: with(beginTransaction()) {
                val fragment =
                    StageFragment.newInstance<MultiPlayControlStageFragment>(startingMatrix)
                fragment.setValueChangedListener(this@AIGameActivity)
                replace(R.id.layout_user, fragment, tag).runOnCommit { on(fragment) }
            }.commit()
    }


    private fun releaseControlBoard() = with(supportFragmentManager) {
        findFragmentByTag(MultiPlayControlStageFragment::class.java.simpleName)
            ?.also { with(beginTransaction()) { remove(it) }.commit() }
    }


    fun viewerBoard(on: (MultiPlayViewerStageFragment) -> Unit) = with(supportFragmentManager) {
        val tag = MultiPlayViewerStageFragment::class.java.simpleName
        findFragmentByTag(tag)?.run { this as? MultiPlayViewerStageFragment }
            ?.also(on)
            ?: with(beginTransaction()) {
                val fragment =
                    StageFragment.newInstance<MultiPlayViewerStageFragment>(startingMatrix)
                replace(R.id.layout_enemy, fragment, tag).runOnCommit { on(fragment) }
            }.commit()
    }

    private fun releaseViewerBoard() = with(supportFragmentManager) {
        findFragmentByTag(MultiPlayViewerStageFragment::class.java.simpleName)
            ?.also { with(beginTransaction()) { remove(it) }.commit() }
    }

    fun stopTimer() {
        recordViewModel.stop()
    }

    override fun onChanged(cell: IntCoordinateCellEntity) {
        controlBoard {
            if (it.isCleared()) {
                stopTimer()


                val record = it.getClearTime()
//                battleViewModel.clear(record)
                showCompleteRecordDialog(record)
            }
        }
    }



    fun setUserProfile(profile: ProfileEntity?) = if (profile != null) {
        profile.takeIf { it != lastKnownUserProfile }
            ?.run {
                profile.run {
                    binding.ivUserIcon.load(
                        iconUrl,
                        errorIcon = getDrawableCompat(R.drawable.ic_person)
                    )
                    binding.tvUserDisplayName.text = displayName
                    binding.tvUserMessage.isVisible = message?.isNotEmpty() == true
                    binding.tvUserMessage.text = message
                }
                lastKnownUserProfile = this
            }
    } else {
        with(binding) {
            ivUserIcon.setImageDrawable(null)
            tvUserDisplayName.text = null
            tvUserMessage.text = null
        }

        lastKnownUserProfile = null
    }

    private fun showCompleteRecordDialog(clearRecord: Long) {
        val dlgBinding =
            LayoutCompleteBinding.inflate(LayoutInflater.from(this))
        dlgBinding.tvRecord.text = clearRecord.toTimerFormat()
        dlgBinding.lottieAnim.playAnimation()
        MaterialAlertDialogBuilder(this)
            .setView(dlgBinding.root)
            .setPositiveButton(R.string.confirm) { _, _ ->
//                isExitAfterCleared = true
//                battleViewModel.exit()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.exit_confirm_for_single)
            .setPositiveButton(R.string.confirm) { _, _ ->
//                if (battleViewModel.isClosedBattle()) {
                finish()
//                } else {
//                    battleViewModel.exit()
//                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }
}