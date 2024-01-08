package kr.co.hs.sudoku.extension.platform

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.extension.platform.ContextExtension.dataStore

object FragmentExtension {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- ProgressIndicator -------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    var Fragment.isShowProgressIndicator: Boolean
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
    fun Fragment.showProgressIndicator() {
        if (isShowingProgressIndicator())
            return

        getRootViewGroup()
            ?.createLayoutForProgressIndicator()
            ?.createProgressIndicator()
    }


    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment Progress Indicator가 표시되고 있는지 여부
     * @return 표시여부 Boolean
     **/
    private fun Fragment.isShowingProgressIndicator() =
        getRootViewGroup()?.findProgressIndicator() != null

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/06
     * @comment Activity의 최상위 rootView 의 Group 리턴
     * @return ViewGroup
     **/
    private fun Fragment.getRootViewGroup() = view?.parent as? ViewGroup

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
            addView(
                it,
                LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                    .apply { setMargins(0, 0, 0, 100.dp.toInt()) }
            )
        }

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment ProgressIndicator 를 제거
     **/
    fun Fragment.dismissProgressIndicator() = removeLayoutForProgressIndicator()

    private fun Fragment.removeLayoutForProgressIndicator() = getRootViewGroup()
        ?.run {
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
    //----------------------------------------- SnackBar -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    fun Fragment.showSnackBar(message: String) = requireActivity().showSnackBar(message)

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- Preference ----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    val Fragment.dataStore: DataStore<Preferences>
        get() = requireContext().dataStore

    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    fun Fragment.getDrawable(res: Int) = ContextCompat.getDrawable(requireContext(), res)
}