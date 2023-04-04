package kr.co.hs.sudoku

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.databinding.ViewpagerSelectLevelBinding
import kr.co.hs.sudoku.model.stage.Stage
import kr.co.hs.sudoku.repository.AdvancedStageRepositoryImpl
import kr.co.hs.sudoku.repository.BeginnerStageRepositoryImpl
import kr.co.hs.sudoku.repository.IntermediateStageRepositoryImpl
import kr.co.hs.sudoku.viewmodel.LevelInfoViewModel

class SelectLevelActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_DIFFICULTY = "kr.co.hs.sudoku.EXTRA_DIFFICULTY"
        const val DIFFICULTY_BEGINNER = "kr.co.hs.sudoku.EXTRA_DIFFICULTY.DIFFICULTY_BEGINNER"
        const val DIFFICULTY_INTERMEDIATE =
            "kr.co.hs.sudoku.EXTRA_DIFFICULTY.DIFFICULTY_INTERMEDIATE"
        const val DIFFICULTY_ADVANCED = "kr.co.hs.sudoku.EXTRA_DIFFICULTY.DIFFICULTY_ADVANCED"
    }

    private lateinit var binding: ViewpagerSelectLevelBinding
    private val viewModel: LevelInfoViewModel
            by viewModels { LevelInfoViewModel.Factory(getStageRepository()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.viewpager_select_level)
        binding.lifecycleOwner = this

        binding.viewPager.offscreenPageLimit = 1

        viewModel.stageList.observe(this) {
            binding.viewPager.adapter = PagerAdapter(supportFragmentManager, lifecycle, it)
            binding.viewPagerIndicator.attachTo(binding.viewPager)
        }

        lifecycleScope.launch {
            withStarted {
                launch { doRequestStageList() }
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 조건에 맞는 StageRepository 를 리턴
     * @return StageRepository
     **/
    private fun getStageRepository() = when (intent.getStringExtra(EXTRA_DIFFICULTY)) {
        DIFFICULTY_INTERMEDIATE -> IntermediateStageRepositoryImpl()
        DIFFICULTY_ADVANCED -> AdvancedStageRepositoryImpl()
        else -> BeginnerStageRepositoryImpl()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스테이지 선택 ViewPager
     * @param fragmentManager
     * @param lifecycle
     * @param stageList remote에서 불러온 스테이지 리스트
     **/
    private class PagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        val stageList: List<Stage>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount() = stageList.size
        override fun createFragment(position: Int) = LevelInfoFragment.newInstance(position)
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 스테이지 리스트 요청
     **/
    private suspend fun doRequestStageList() {
        binding.progress.show()
        withContext(Dispatchers.IO) { fetchRemoteConfig() }
        viewModel.doRequestStageList()
        binding.progress.hide()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment fetchRemoteConfig
     * @return 성공여부 Boolean
     **/
    private suspend fun fetchRemoteConfig() = getRemoteConfig().fetchAndActivate().await()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment RemoteConfig
     * @return RemoteConfig
     **/
    private fun getRemoteConfig() = FirebaseRemoteConfig.getInstance()
}