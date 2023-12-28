package kr.co.hs.sudoku.core

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kr.co.hs.sudoku.R

abstract class Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
    }

    protected fun navigateUpToParent() =
        NavUtils.getParentActivityIntent(this)
            ?.let { parentActivityIntent ->
                val bundle = Bundle()
                onNavigateUpToParent(bundle)
                parentActivityIntent.putExtras(bundle)
                NavUtils.navigateUpTo(this, parentActivityIntent)
            }
            ?: runCatching { NavUtils.navigateUpFromSameTask(this) }
                .onFailure { finish() }

    protected open fun onNavigateUpToParent(bundle: Bundle) {}

    inline fun showAlert(
        titleResId: Int? = null,
        msgResId: Int,
        crossinline onClosed: () -> Unit
    ): AlertDialog = with(MaterialAlertDialogBuilder(this)) {
        titleResId?.run { setTitle(this) }
        setMessage(msgResId)
        setPositiveButton(R.string.confirm) { _, _ -> onClosed() }
        setOnDismissListener { onClosed() }
    }.show()

    inline fun showConfirm(
        titleResId: Int? = null,
        msgResId: Int,
        crossinline onConfirm: (Boolean) -> Unit
    ): AlertDialog = with(MaterialAlertDialogBuilder(this)) {
        titleResId?.run { setTitle(this) }
        setMessage(msgResId)
        setPositiveButton(R.string.confirm) { _, _ -> onConfirm(true) }
        setNegativeButton(R.string.cancel) { _, _ -> onConfirm(false) }
        setOnDismissListener { onConfirm(false) }
    }.show()

    protected fun Throwable.showErrorAlert(): AlertDialog =
        with(MaterialAlertDialogBuilder(this@Activity)) {
            setMessage(this@showErrorAlert.message.toString())
            setPositiveButton(R.string.confirm) { _, _ -> }
            setOnDismissListener { }
        }.show()
}