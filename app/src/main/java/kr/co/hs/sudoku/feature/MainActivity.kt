package kr.co.hs.sudoku.feature

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.games.PlayGames
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.feature.ad.AppOpenAdManager
import kr.co.hs.sudoku.feature.ad.NativeItemAdManager
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityMainBinding
import kr.co.hs.sudoku.extension.CoilExt.appImageLoader
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.Bitmap.toCropCircle
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat
import kr.co.hs.sudoku.feature.challenge.dashboard.ChallengeDashboardFragment
import kr.co.hs.sudoku.feature.challenge.dashboard.ChallengeDashboardViewModel
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardFragment
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardViewModel
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel
import kr.co.hs.sudoku.feature.profile.ProfileUpdateActivity
import kr.co.hs.sudoku.feature.single.SingleDashboardFragment
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : Activity(), NavigationBarView.OnItemSelectedListener {

    companion object {
        const val EXTRA_CURRENT_TAB_ITEM_ID = "EXTRA_CURRENT_TAB_ITEM_ID"
    }

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val app: App by lazy { applicationContext as App }
    private val multiPlayViewModel: MultiPlayViewModel by viewModels {
        MultiPlayViewModel.ProviderFactory(app.getBattleRepository())
    }
    private val multiDashboardViewModel: MultiDashboardViewModel by viewModels {
        MultiDashboardViewModel.ProviderFactory(app.getBattleRepository(), NativeItemAdManager(app))
    }
    private val challengeDashboardViewMode: ChallengeDashboardViewModel by viewModels {
        ChallengeDashboardViewModel.ProviderFactory(app.getChallengeRepository())
    }
    private val userProfileViewModel: UserProfileViewModel by viewModels {
        UserProfileViewModel.ProviderFactory(
            app.getProfileRepository(),
            PlayGames.getGamesSignInClient(this),
            getString(R.string.default_web_client_id)
        )
    }
    private val gameSettingsViewModel: GameSettingsViewModel by viewModels {
        GameSettingsViewModel.Factory(app.getGameSettingsRepository())
    }
    private val launcherForProfileUpdate =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                userProfileViewModel.requestCurrentUserProfile()
            }
        }
    private var hasUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)

        multiPlayViewModel.error.observe(this) { it.showErrorAlert() }
        with(multiDashboardViewModel) {
            error.observe(this@MainActivity) { it.showErrorAlert() }
            currentMultiPlay.observe(this@MainActivity) {
                supportActionBar?.subtitle = when (it) {
                    is BattleEntity.Opened,
                    is BattleEntity.Pending -> getString(R.string.multi_noti_waiting)

                    is BattleEntity.Playing -> getString(R.string.multi_noti_playing)
                    else -> null
                }
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) { checkCurrentMultiPlay() }
            }
        }
        challengeDashboardViewMode.error.observe(this) { it.showErrorAlert() }
        with(userProfileViewModel) {
            error.observe(this@MainActivity) { it.showErrorAlert() }
            isRunningProgress.observe(this@MainActivity) {
                it.takeIf { it }?.run { supportActionBar?.setUIProfileLoading() }
            }
            profile.observe(this@MainActivity) {
                supportActionBar?.setUIProfile(it)
                invalidateOptionsMenu()
            }
            // Play Games에논 로그인이 되어 있는데 Firebase 인증이 되어 있지 않은 경우가 있을 수 있어서 마이그레이션
            lifecycleScope.launch { withStarted { requestCurrentUserProfile() } }
        }

        with(gameSettingsViewModel) {
            error.observe(this@MainActivity) { it.showErrorAlert() }
            gameSettings.observe(this@MainActivity) { invalidateOptionsMenu() }
        }


        // BottomNavigationView 아이템 선택 리스너 등록
        with(binding.bottomNavigationView) {
            setOnItemSelectedListener(this@MainActivity)
            selectedItemId =
                savedInstanceState?.getInt(EXTRA_CURRENT_TAB_ITEM_ID) ?: R.id.menu_single
        }

        if (savedInstanceState == null) {
            val isFirstAppOpened = runBlocking {
                app.getRegistrationRepository().isFirstAppOpened()
            }
            when (isFirstAppOpened) {
                true -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        app.getRegistrationRepository().appOpened()
                    }
                }

                false -> AppOpenAdManager(this@MainActivity).showIfAvailable()
            }
        }

        checkUpdate()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Content Fragment 교체
     * @param fragment 교체할 Fragment
     **/
    override fun onNavigationItemSelected(item: MenuItem) = with(item) {
        when (itemId) {
            R.id.menu_single -> {
                showSinglePlayDashboard()
                lifecycleScope.launch {
                    val hasSeenSinglePlayGuide = withContext(Dispatchers.IO) {
                        app.getRegistrationRepository().hasSeenSinglePlayGuide()
                    }
                    if (!hasSeenSinglePlayGuide) {
                        showSingleTabGuide(lifecycle)
                    }
                }
            }

            R.id.menu_multi -> {
                showMultiPlayDashboard()
                lifecycleScope.launch {
                    val hasSeenMultiPlayGuide = withContext(Dispatchers.IO) {
                        app.getRegistrationRepository().hasSeenMultiPlayGuide()
                    }
                    if (!hasSeenMultiPlayGuide) {
                        showMultiTabGuide(lifecycle)
                    }
                }
            }

            R.id.challenge -> {
                showChallengeDashboard()
                lifecycleScope.launch {
                    val hasSeenChallengeGuide = withContext(Dispatchers.IO) {
                        app.getRegistrationRepository().hasSeenChallengeGuide()
                    }
                    if (!hasSeenChallengeGuide) {
                        showChallengeTabGuide(lifecycle)
                    }
                }
            }
        }
        true
    }

    private fun <T : Fragment> showTabContents(fragment: T) = with(supportFragmentManager) {
        val fragmentName = fragment::class.java.name
        if (findFragmentByTag(fragmentName) == null) {
            with(beginTransaction()) {
                replace(R.id.layout_contents, fragment, fragmentName)
                commit()
            }
        }
    }

    private fun showSinglePlayDashboard() = showTabContents(SingleDashboardFragment.newInstance())
    private fun showMultiPlayDashboard() = showTabContents(MultiDashboardFragment.newInstance())
    private fun showChallengeDashboard() = showTabContents(ChallengeDashboardFragment.newInstance())

    private fun ActionBar.setUIProfileLoading() {
        title = getString(R.string.please_wait_profile)
        val drawable = object : CircularProgressDrawable(themedContext) {
            override fun getIntrinsicWidth() = 24.dp.toInt()
            override fun getIntrinsicHeight() = 24.dp.toInt()
            override fun getStrokeWidth() = 2.dp
            override fun getColorSchemeColors() =
                intArrayOf(ContextCompat.getColor(this@MainActivity, R.color.gray_500))
        }
        setLogo(drawable)
        drawable.start()
    }

    private fun ActionBar.setUIProfile(profileEntity: ProfileEntity?) = profileEntity
        ?.run {
            title = getString(R.string.welcome_format, displayName)
            loadIcon(URL(iconUrl), getDrawableCompat(R.drawable.ic_person))
        }
        ?: run {
            title = getString(R.string.welcome_not_login)
            setLogo(R.drawable.ic_person)
        }

    private fun ActionBar.loadIcon(data: URL, errorIcon: Drawable? = null) =
        appImageLoader.enqueue(
            ImageRequest.Builder(themedContext)
                .data(data.toString())
                .error(errorIcon)
                .target(
                    onStart = onStartLoadIcon,
                    onSuccess = onSuccessLoadIcon,
                    onError = onErrorLoadIcon
                )
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()
        )

    private val ActionBar.onStartLoadIcon: (Drawable?) -> Unit
        get() = { _ ->
            val drawable = object : CircularProgressDrawable(themedContext) {
                override fun getIntrinsicWidth() = 24.dp.toInt()
                override fun getIntrinsicHeight() = 24.dp.toInt()
                override fun getStrokeWidth() = 2.dp
                override fun getColorSchemeColors() =
                    intArrayOf(ContextCompat.getColor(this@MainActivity, R.color.gray_500))
            }
            setLogo(drawable)
            drawable.start()
        }

    private val ActionBar.onSuccessLoadIcon: (Drawable) -> Unit
        get() = { icon ->
            val bitmapIcon = (icon as BitmapDrawable).bitmap.toCropCircle()
            val bitmap = Bitmap.createScaledBitmap(bitmapIcon, 24.dp.toInt(), 24.dp.toInt(), true)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            paint.color = getColorCompat(R.color.gray_600)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.dp
            canvas.drawCircle(12.dp, 12.dp, 11.5f.dp, paint)
            val bitmap2 = BitmapDrawable(resources, bitmap)
            setLogo(bitmap2)
        }

    private val ActionBar.onErrorLoadIcon: (Drawable?) -> Unit
        get() = { error ->
            val d = object : DrawableWrapper(error) {
                override fun getIntrinsicWidth() = 24.dp.toInt()
                override fun getIntrinsicHeight() = 24.dp.toInt()
            }
            setLogo(d)
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_option, menu)

        menu?.findItem(R.id.sign_in)?.isVisible = userProfileViewModel.profile.value == null
        menu?.findItem(R.id.edit_profile)?.isVisible = userProfileViewModel.profile.value != null

        menu?.findItem(R.id.enabled_cell_haptic)?.isChecked =
            gameSettingsViewModel.gameSettings.value?.enabledHapticFeedback == true

        menu?.findItem(R.id.version_info)?.title = packageManager.runCatching {
            val packageInfo = getPackageInfo(packageName, PackageManager.GET_META_DATA)
            if (hasUpdate) {
                getString(R.string.version_updatable_format, packageInfo.versionName)
            } else {
                getString(R.string.version_format, packageInfo.versionName)
            }
        }.getOrDefault("")

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_in -> {
                userProfileViewModel.signIn()
                true
            }

            R.id.edit_profile -> {
                launcherForProfileUpdate.launch(ProfileUpdateActivity.newIntent(this))
                true
            }

            R.id.enabled_cell_haptic -> {
                val currentSettings = !item.isChecked
                item.isChecked = currentSettings
                gameSettingsViewModel.gameSettings.value?.let { gameSettings ->
                    gameSettings.enabledHapticFeedback = currentSettings
                    gameSettingsViewModel.setGameSettings(gameSettings)
                }
                true
            }

            R.id.clear_seen_guide -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { app.getRegistrationRepository().clear() }
                    showAlert(null, R.string.clear_seen_guide_success) {}
                }
                true
            }

            R.id.version_info -> {
                lifecycleScope.launch { doUpdate() }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_CURRENT_TAB_ITEM_ID, binding.bottomNavigationView.selectedItemId)
    }


    /**
     * in-app update
     */
    private fun checkUpdate() = lifecycleScope.launch {
        withStarted {
            launch {
                if (getUpdateInfo()?.hasUpdate() == true) {
                    hasUpdate = true
                    showConfirm(R.string.in_app_update_title, R.string.in_app_update_message) {
                        if (it) {
                            lifecycleScope.launch { doUpdate() }
                        }
                    }
                } else {
                    hasUpdate = false
                }
                invalidateOptionsMenu()
            }
        }
    }

    private suspend fun getUpdateInfo() = AppUpdateManagerFactory.create(this)
        .runCatching { appUpdateInfo.await() }
        .getOrNull()

    private fun AppUpdateInfo.hasUpdate() =
        updateAvailability() == UPDATE_AVAILABLE && isUpdateTypeAllowed(IMMEDIATE)

    private val launcherForInAppUpdate =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                showAlert(R.string.in_app_update_title, R.string.in_app_update_message_confirm) {}
            } else {
                showAlert(R.string.in_app_update_title, R.string.in_app_update_message_canceled) {}
            }
        }

    private suspend fun doUpdate() = getUpdateInfo()
        ?.let { updateInfo ->
            AppUpdateManagerFactory.create(this).startUpdateFlowForResult(
                updateInfo,
                launcherForInAppUpdate,
                AppUpdateOptions.newBuilder(IMMEDIATE).build()
            )
        }


    /**
     * 탭별 가이드 표시
     */
    private suspend fun showSingleTabGuide(lifecycle: Lifecycle, onDismiss: (() -> Unit)? = null) {
        val bounds = binding.bottomNavigationView.getTabBounds(0)
        TapTargetView.showFor(
            this,
            TapTarget.forBounds(
                bounds,
                getString(R.string.single_tab_guide_title),
                getString(R.string.single_tab_guide_desc)
            ).apply {
                cancelable(false)
                transparentTarget(true)
                outerCircleColor(R.color.gray_700)
            },
            object : TapTargetView.Listener() {
                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    lifecycle.coroutineScope.launch(Dispatchers.IO) {
                        app.getRegistrationRepository().seenSinglePlayGuide()
                    }
                    onDismiss?.invoke()
                }
            }
        )
    }

    private suspend fun showMultiTabGuide(lifecycle: Lifecycle, onDismiss: (() -> Unit)? = null) {
        val bounds = binding.bottomNavigationView.getTabBounds(1)
        TapTargetView.showFor(
            this,
            TapTarget.forBounds(
                bounds,
                getString(R.string.multi_tab_guide_title),
                getString(R.string.multi_tab_guide_desc)
            ).apply {
                cancelable(false)
                transparentTarget(true)
                outerCircleColor(R.color.gray_700)
            },
            object : TapTargetView.Listener() {
                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    lifecycle.coroutineScope.launch(Dispatchers.IO) {
                        app.getRegistrationRepository().seenMultiPlayGuide()
                    }
                    onDismiss?.invoke()
                }
            }
        )
    }

    private suspend fun showChallengeTabGuide(
        lifecycle: Lifecycle,
        onDismiss: (() -> Unit)? = null
    ) {
        val bounds = binding.bottomNavigationView.getTabBounds(2)
        TapTargetView.showFor(
            this,
            TapTarget.forBounds(
                bounds,
                getString(R.string.challenge_tab_guide_title),
                getString(R.string.challenge_tab_guide_desc)
            ).apply {
                cancelable(false)
                transparentTarget(true)
                outerCircleColor(R.color.gray_700)
            },
            object : TapTargetView.Listener() {
                override fun onTargetDismissed(view: TapTargetView?, userInitiated: Boolean) {
                    lifecycle.coroutineScope.launch(Dispatchers.IO) {
                        app.getRegistrationRepository().seenChallengeGuide()
                    }
                    onDismiss?.invoke()
                }
            }
        )
    }

    private suspend fun BottomNavigationView.getTabBounds(position: Int) =
        suspendCoroutine { emit ->
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val tabView = (children.first() as ViewGroup).children.toList()[position]
                    val location = IntArray(2)
                    tabView.getLocationOnScreen(location)
                    emit.resume(
                        Rect(
                            location[0],
                            location[1],
                            location[0] + tabView.width,
                            location[1] + tabView.height
                        )
                    )
                }
            }
            )
        }
}