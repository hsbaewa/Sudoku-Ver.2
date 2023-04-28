package kr.co.hs.sudoku.feature.level

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ViewpagerSelectLevelBinding
import kr.co.hs.sudoku.extension.firebase.RemoteConfigExt.fetchAndActivate
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.model.matrix.IntMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.viewmodel.SinglePlayDifficultyViewModel

class LevelActivity : Activity() {
    companion object {
        fun Activity.startSelectLevel(difficulty: Difficulty) =
            startActivity(
                Intent(this, LevelActivity::class.java)
                    .putDifficulty(difficulty)
            )
    }

    private val binding: ViewpagerSelectLevelBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.viewpager_select_level) }
    private val viewModel: SinglePlayDifficultyViewModel
            by lazy { singlePlayDifficultyViewModels() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this

        binding.toolbar.setupUI()
        binding.appbarLayout.setupUI()

        viewModel.matrixList.observe(this) {
            dismissProgressIndicator()
            binding.viewPager.adapter = PagerAdapter(supportFragmentManager, lifecycle, it)
            binding.viewPagerIndicator.attachTo(binding.viewPager)
        }


        lifecycleScope.launch(CoroutineExceptionHandler { _, t ->
            showSnackBar(t.message.toString())
            dismissProgressIndicator()
        }) {
            // remoteconfig 업데이트
            withContext(Dispatchers.IO) { fetchAndActivate() }

            withStarted {
                showProgressIndicator()
                // 스테이지 리스트 요청
                viewModel.requestMatrix(
                    when (getDifficulty()) {
                        Difficulty.BEGINNER -> BeginnerMatrixRepository()
                        Difficulty.INTERMEDIATE -> IntermediateMatrixRepository()
                        Difficulty.ADVANCED -> AdvancedMatrixRepository()
                    }
                )
            }
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Toolbar 설정
     **/
    private fun Toolbar.setupUI() {
        setSupportActionBar(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment AppBarLayout 설정
     **/
    private fun AppBarLayout.setupUI() {
        // 투명한 AppBarLayout을 만들기 위해 outlineProvider를 null로 설정
        outlineProvider = null
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment 옵션 클릭 이벤트로 back 버튼 클릭 이벤트때 화면 종료하기 위해 오버라이딩
     **/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/03
     * @comment 스테이지 선택 ViewPager
     * @param fragmentManager
     * @param lifecycle
     * @param sudokuGenerateMatrixList remote에서 불러온 스테이지 리스트로 매트릭스의 value가 0이 아닌 값이 있으면 해당 포지션은 고정값
     **/
    private class PagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        val sudokuGenerateMatrixList: List<IntMatrix>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount() = sudokuGenerateMatrixList.size
        override fun createFragment(position: Int) = LevelFragment.new(position)
    }

}