package kr.co.hs.sudoku.extension.platform

import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.fragment.app.Fragment
import kr.co.hs.sudoku.extension.platform.ActivityExtension.dismissProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showProgressIndicator
import kr.co.hs.sudoku.extension.platform.ActivityExtension.showSnackBar
import kr.co.hs.sudoku.extension.platform.ContextExtension.dataStore

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