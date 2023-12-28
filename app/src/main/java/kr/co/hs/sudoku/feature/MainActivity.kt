package kr.co.hs.sudoku.feature

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.gms.games.PlayGames
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ActivityMainBinding
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

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val app: App by lazy { applicationContext as App }
    private val multiPlayViewModel: MultiPlayViewModel by viewModels {
        MultiPlayViewModel.ProviderFactory(app.getBattleRepository())
    }
    private val multiDashboardViewModel: MultiDashboardViewModel by viewModels {
        MultiDashboardViewModel.ProviderFactory(app.getBattleRepository())
    }
    private val challengeDashboardViewMode: ChallengeDashboardViewModel by viewModels {
        ChallengeDashboardViewModel.ProviderFactory(app.getChallengeRepository())
    }
    private val userProfileViewModel: UserProfileViewModel by viewModels {
        UserProfileViewModel.ProviderFactory(
            PlayGames.getGamesSignInClient(this),
            getString(R.string.default_web_client_id)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        multiPlayViewModel.error.observe(this) { it.showErrorAlert() }
        multiDashboardViewModel.error.observe(this) { it.showErrorAlert() }
        challengeDashboardViewMode.error.observe(this) { it.showErrorAlert() }
        with(userProfileViewModel) {
            error.observe(this@MainActivity) {
                it.showErrorAlert()
            }
            isRunningProgress.observe(this@MainActivity) { isShowProgressIndicator = it }
        }

        // BottomNavigationView 아이템 선택 리스너 등록
        with(binding.bottomNavigationView) {
            setOnItemSelectedListener(this@MainActivity)
            selectedItemId = R.id.menu_single
        }

        // Play Games에논 로그인이 되어 있는데 Firebase 인증이 되어 있지 않은 경우가 있을 수 있어서 마이그레이션
        lifecycleScope.launch {
            withStarted { userProfileViewModel.requestCurrentUserProfile() }
        }
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
            R.id.settings -> showSettings()
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
    private fun showSettings() = showTabContents(SettingsFragment.newInstance())
}