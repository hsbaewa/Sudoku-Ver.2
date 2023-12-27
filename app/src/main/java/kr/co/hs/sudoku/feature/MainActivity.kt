package kr.co.hs.sudoku.feature

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.games.PlayGames
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.auth.FirebaseAuthMediatorImpl
import kr.co.hs.sudoku.databinding.ActivityMainBinding
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.replaceFragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.feature.settings.SettingsFragment
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.feature.challenge.dashboard.ChallengeDashboardFragment
import kr.co.hs.sudoku.feature.challenge.dashboard.ChallengeDashboardViewModel
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardFragment
import kr.co.hs.sudoku.feature.multi.dashboard.MultiDashboardViewModel
import kr.co.hs.sudoku.feature.single.SingleDashboardFragment
import kr.co.hs.sudoku.feature.multi.play.MultiPlayViewModel

class MainActivity : Activity(), NavigationBarView.OnItemSelectedListener {

    private val binding: ActivityMainBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_main) }

    private val multiPlayViewModel: MultiPlayViewModel by viewModels {
        val app = applicationContext as App
        MultiPlayViewModel.ProviderFactory(app.getBattleRepository())
    }

    private val multiDashboardViewModel: MultiDashboardViewModel by viewModels {
        val app = applicationContext as App
        MultiDashboardViewModel.ProviderFactory(app.getBattleRepository())
    }
    private val challengeDashboardViewMode: ChallengeDashboardViewModel by viewModels {
        val app = applicationContext as App
        ChallengeDashboardViewModel.ProviderFactory(app.getChallengeRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multiPlayViewModel.error.observe(this) { showSnackBar(it.message.toString()) }
        multiDashboardViewModel.error.observe(this) { showSnackBar(it.message.toString()) }
        multiDashboardViewModel.isRunningProgress.observe(this) { isShowProgressIndicator = it }
        challengeDashboardViewMode.error.observe(this) { showSnackBar(it.message.toString()) }
        challengeDashboardViewMode.isRunningProgress.observe(this) { isShowProgressIndicator = it }
        binding.lifecycleOwner = this

        // 하단에 있는 BottomNavigationView 와 상단에 내용이 표시될 Layout과 상호 작용
//        replaceTabFragment(DifficultyFragment.new())

        // BottomNavigationView 아이템 선택 리스너 등록
        bottomNavigationBar.setOnItemSelectedListener(this)
        bottomNavigationBar.selectedItemId = R.id.menu_single

        // Play Games에논 로그인이 되어 있는데 Firebase 인증이 되어 있지 않은 경우가 있을 수 있어서 마이그레이션
        lifecycleScope.launch(coroutineExceptionHandler) {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                showProgressIndicator()
                withContext(Dispatchers.IO) { doCheckAuthenticate() }
                dismissProgressIndicator()
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Content Fragment 교체
     * @param fragment 교체할 Fragment
     **/
    private fun replaceTabFragment(fragment: Fragment) =
        replaceFragment(R.id.tabContentLayout, fragment)

    private val bottomNavigationBar by lazy { binding.navigationBar }

    override fun onNavigationItemSelected(item: MenuItem) = with(item) {
        when (itemId) {
            R.id.menu_single -> replaceTabFragment(SingleDashboardFragment.newInstance())
            R.id.menu_multi -> replaceTabFragment(MultiDashboardFragment.newInstance())
            R.id.challenge -> replaceTabFragment(ChallengeDashboardFragment.newInstance())
            R.id.settings -> replaceTabFragment(SettingsFragment.new())
        }
        true
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        dismissProgressIndicator()
        showSnackBar(t.message.toString())
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/17
     * @comment Play Games에논 로그인이 되어 있는데 Firebase 인증이 되어 있지 않은 경우가 있을 수 있어서 마이그레이션
     **/
    private suspend fun doCheckAuthenticate() = with(createAuthenticateMediator()) {
        takeIf { it.needMigrationWithPlayGames() }
            ?.run { migrationWithPlayGames() }
            ?.run {
                runCatching { getProfile(uid) }
                    .onFailure {
                        if (it is NullPointerException) {
                            updateProfile(toDomain())
                        }
                    }
            }
    }

    private fun createAuthenticateMediator() = FirebaseAuthMediatorImpl(
        FirebaseAuth.getInstance(),
        PlayGames.getGamesSignInClient(this),
        getString(R.string.default_web_client_id)
    )
}