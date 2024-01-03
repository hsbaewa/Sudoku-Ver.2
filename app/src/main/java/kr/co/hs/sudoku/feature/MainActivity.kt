package kr.co.hs.sudoku.feature

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withStarted
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.android.gms.games.PlayGames
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.AppOpenAdManager
import kr.co.hs.sudoku.NativeItemAdManager
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityMainBinding
import kr.co.hs.sudoku.extension.CoilExt.appImageLoader
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.Bitmap.toCropCircle
import kr.co.hs.sudoku.extension.platform.ContextExtension.dataStore
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
import kr.co.hs.sudoku.model.settings.GameSettingsEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.viewmodel.GameSettingsViewModel
import java.net.URL


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
        GameSettingsViewModel.Factory(GameSettingsRepositoryImpl(dataStore))
    }
    private val launcherForProfileUpdate =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                userProfileViewModel.requestCurrentUserProfile()
            }
        }

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

        AppOpenAdManager(this).showIfAvailable()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Content Fragment 교체
     * @param fragment 교체할 Fragment
     **/
    override fun onNavigationItemSelected(item: MenuItem) = with(item) {
        when (itemId) {
            R.id.menu_single -> showSinglePlayDashboard()
            R.id.menu_multi -> showMultiPlayDashboard()
            R.id.challenge -> showChallengeDashboard()
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
            getString(R.string.version_format, packageInfo.versionName, packageInfo.versionCode)
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
                gameSettingsViewModel.setGameSettings(GameSettingsEntity(enabledHapticFeedback = currentSettings))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_CURRENT_TAB_ITEM_ID, binding.bottomNavigationView.selectedItemId)
    }
}