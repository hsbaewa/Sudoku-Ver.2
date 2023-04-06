package kr.co.hs.sudoku.extension

import androidx.fragment.app.Fragment
import kr.co.hs.sudoku.extension.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.ActivityExtension.showSnackBar

object FragmentExtension {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- ProgressIndicator -------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment ProgressIndicator 표시
     **/
    fun Fragment.showProgressIndicator() = requireActivity().showProgressIndicator()

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/05
     * @comment ProgressIndicator 를 제거
     **/
    fun Fragment.dismissProgressIndicator() = requireActivity().dismissProgressIndicator()


    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- SnackBar -----------------------------------------\\
    //--------------------------------------------------------------------------------------------\\
    fun Fragment.showSnackBar(message: String) = requireActivity().showSnackBar(message)
}