package kr.co.hs.sudoku.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        replaceTabFragment(SelectStageFragment.newInstance())
        binding.navigationBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.selectStage -> replaceTabFragment(SelectStageFragment.newInstance())
            }
            return@setOnItemSelectedListener true
        }
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/04
     * @comment Content Fragment 교체
     * @param fragment 교체할 Fragment
     **/
    private fun replaceTabFragment(fragment: Fragment) =
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.tabContentLayout, fragment)
            commit()
        }
}