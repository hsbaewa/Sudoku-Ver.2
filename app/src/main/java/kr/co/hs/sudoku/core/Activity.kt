package kr.co.hs.sudoku.core

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.games.PlayGames
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import kr.co.hs.sudoku.feature.messaging.MessagingManager.Action.Companion.parseAction
import kr.co.hs.sudoku.feature.profile.UserProfileViewModel
import kr.co.hs.sudoku.repository.battle.BattleRepositoryImpl

abstract class Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        intent.extras
            ?.run { RemoteMessage(this) }
            ?.run { onReceivedRemoteMessage(this) }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.extras
            ?.run { RemoteMessage(this) }
            ?.run { onReceivedRemoteMessage(this) }
    }

    private fun onReceivedRemoteMessage(remoteMessage: RemoteMessage) {
        when (remoteMessage.parseAction()) {
            is MessagingManager.AppUpdate -> lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
                dismissProgressIndicator()
                throwable.showErrorAlert()
            }) {
                showProgressIndicator()
                doUpdate()
            }

            else -> {}
        }
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

    inline fun showAlert(
        title: String? = null,
        msg: String,
        crossinline onClosed: () -> Unit
    ): AlertDialog = with(MaterialAlertDialogBuilder(this)) {
        title?.run { setTitle(this) }
        setMessage(msg)
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

    inline fun showConfirm(
        title: String? = null,
        msg: String,
        crossinline onConfirm: (Boolean) -> Unit
    ): AlertDialog = with(MaterialAlertDialogBuilder(this)) {
        title?.run { setTitle(this) }
        setMessage(msg)
        setPositiveButton(R.string.confirm) { _, _ -> onConfirm(true) }
        setNegativeButton(R.string.cancel) { _, _ -> onConfirm(false) }
        setOnDismissListener { onConfirm(false) }
    }.show()

    fun getErrorMessage(t: Throwable) = when (t) {
        is BattleRepositoryImpl.BattleRepositoryException -> {
            when (t.type) {
                BattleRepositoryImpl.EmptyParticipant -> getString(R.string.multi_play_error_empty_participants)
                BattleRepositoryImpl.RequireReadyAllUsers -> getString(R.string.multi_play_error_require_ready_all_users)
                BattleRepositoryImpl.AlreadyFull -> getString(R.string.multi_list_error_already_full)
                BattleRepositoryImpl.NotFound -> getString(R.string.multi_list_not_found)
                BattleRepositoryImpl.OnlyHost -> getString(R.string.multi_play_error_only_host)
                BattleRepositoryImpl.AlreadyHasStarted -> getString(R.string.multi_play_error_already_started)
                else -> t.message.toString()
            }
        }

        else -> t.message.toString()
    }

    protected fun Throwable.showErrorAlert() = showAlert(title.toString(), getErrorMessage(this)) {}


    /**
     * in-app update
     */
    protected suspend fun getUpdateInfo() = AppUpdateManagerFactory.create(this)
        .runCatching { appUpdateInfo.await() }
        .getOrNull()

    protected fun AppUpdateInfo.hasUpdate() =
        updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && isUpdateTypeAllowed(
            AppUpdateType.IMMEDIATE
        )

    protected val launcherForInAppUpdate =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            dismissProgressIndicator()
            if (it.resultCode == RESULT_OK) {
                showAlert(R.string.in_app_update_title, R.string.in_app_update_message_confirm) {}
            } else {
                showAlert(R.string.in_app_update_title, R.string.in_app_update_message_canceled) {}
            }
        }

    protected suspend fun doUpdate() = getUpdateInfo()
        ?.let { updateInfo ->
            AppUpdateManagerFactory.create(this).startUpdateFlowForResult(
                updateInfo,
                launcherForInAppUpdate,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }

    fun getUserProfileProviderFactory() = with(applicationContext as App) {
        UserProfileViewModel.ProviderFactory(
            getProfileRepository(),
            PlayGames.getGamesSignInClient(this@Activity),
            getString(R.string.default_web_client_id),
            getBattleRepository(),
            getChallengeRepository()
        )
    }

}