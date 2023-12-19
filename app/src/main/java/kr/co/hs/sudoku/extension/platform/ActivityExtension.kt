package kr.co.hs.sudoku.extension.platform

import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kr.co.hs.sudoku.extension.NumberExtension.toPx

@Suppress("unused")
object ActivityExtension {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Progress Indicator ------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    var Activity.isShowProgressIndicator: Boolean
        get() = isShowingProgressIndicator()
        set(value) {
            if (value) {
                showProgressIndicator()
            } else {
                dismissProgressIndicator()
            }
        }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment ProgressIndicator 표시
     **/
    fun Activity.showProgressIndicator() {
        if (isShowingProgressIndicator())
            return

        getRootViewGroup()
            .createLayoutForProgressIndicator()
            .createProgressIndicator()
    }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment Progress Indicator가 표시되고 있는지 여부
     * @return 표시여부 Boolean
     **/
    private fun Activity.isShowingProgressIndicator() =
        getRootViewGroup().findProgressIndicator() != null

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment Activity의 최상위 rootView 의 Group 리턴
     * @return ViewGroup
     **/
    private fun Activity.getRootViewGroup() = window.decorView.rootView as ViewGroup

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment ViewGroup에 속한 progressIndicator 찾기
     * @return CircularProgressIndicator
     **/
    private fun ViewGroup.findProgressIndicator() =
        findViewWithTag<CircularProgressIndicator>(TAG_PROGRESS_INDICATOR)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment ProgressIndicator를 구분하기 위한 tag
     **/
    private const val TAG_PROGRESS_INDICATOR = "ProgressIndicator"

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment ProgressIndicator를 담기 위한 Layout을 초기화 하여 ViewGroup에 add한다.
     **/
    private fun ViewGroup.createLayoutForProgressIndicator() =
        LinearLayout(context).apply {
            gravity = Gravity.CENTER
            tag = TAG_LINEAR_LAYOUT_FOR_PROGRESS_INDICATOR
            isClickable = true
            isFocusable = true
        }.also {
            addView(
                it,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

    private const val TAG_LINEAR_LAYOUT_FOR_PROGRESS_INDICATOR = "LinearLayoutForProgressIndicator"


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment ProgressIndicator 생성하여 Layout에 add한다.
     * @return CircularProgressIndicator
     **/
    private fun LinearLayout.createProgressIndicator() =
        CircularProgressIndicator(context).apply {
            isIndeterminate = true
            tag = TAG_PROGRESS_INDICATOR
        }.also {
            addView(it)
        }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment ProgressIndicator 를 제거
     **/
    fun Activity.dismissProgressIndicator() = removeLayoutForProgressIndicator()

    private fun Activity.removeLayoutForProgressIndicator() = with(getRootViewGroup()) {
        findLayoutForProgressIndicator()
            .takeIf { layout -> layout != null }
            ?.let { layout ->

                layout.findProgressIndicator().takeIf { indicator -> indicator != null }
                    ?.let { indicator -> layout.removeView(indicator) }

                removeView(layout)
            }
    }

    private fun ViewGroup.findLayoutForProgressIndicator() =
        findViewWithTag<LinearLayout>(TAG_LINEAR_LAYOUT_FOR_PROGRESS_INDICATOR)


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- 메시지 snackBar 관련 --------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    fun Activity.showSnackBar(message: String) {
        val view = getRootViewGroup()
            .createCoordinatorLayoutForSnackBar()

        val callBack = object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                view.findCoordinatorLayoutForSnackBar().takeIf { it != null }
                    ?.run { view.removeView(this) }
                transientBottomBar?.removeCallback(this)
            }
        }

        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .addCallback(callBack)
            .show()
    }

    private fun ViewGroup.createCoordinatorLayoutForSnackBar() =
        CoordinatorLayout(context).apply {
            tag = TAG_COORDINATOR_LAYOUT_FOR_SNACK_BAR
        }.also {
            addView(
                it,
                ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply { setMargins(10.toPx, 0, 10.toPx, 100.toPx) }
            )
        }

    private const val TAG_COORDINATOR_LAYOUT_FOR_SNACK_BAR = "LayoutForMessage"


    private fun ViewGroup.findCoordinatorLayoutForSnackBar() =
        findViewWithTag<CoordinatorLayout>(TAG_COORDINATOR_LAYOUT_FOR_SNACK_BAR)


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- FragmentTransaction ------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment FragmentTransaction을 이용하여 특정 container에 Fragment를 대체
     * @param
     * @return
     **/
    fun AppCompatActivity.replaceFragment(containerViewId: Int, fragment: Fragment) =
        with(supportFragmentManager.beginTransaction()) {
            replace(containerViewId, fragment, fragment::class.java.simpleName)
            commit()
        }

    inline fun <reified T : Fragment> AppCompatActivity.removeFragment(c: Class<T>) =
        with(supportFragmentManager) {
            findFragmentByTag(c.simpleName)?.let { fragment ->
                val transaction = beginTransaction()
                transaction.remove(fragment)
                transaction.commit()
            }
        }

    inline fun <reified T : Fragment> AppCompatActivity.hasFragment(c: Class<T>) =
        runCatching { findFragment(c) }.getOrNull() != null

    inline fun <reified T : Fragment> AppCompatActivity.findFragment(c: Class<T>): T {
        return with(supportFragmentManager) {
            val name = c.simpleName
            (findFragmentByTag(name) as? T) ?: throw Exception("not found $name")
        }
    }

    @Suppress("DEPRECATION")
    val Activity.screenWidth: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets =
                windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - (insets.left + insets.right)
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
}