package kr.co.hs.sudoku.core

abstract class Fragment : androidx.fragment.app.Fragment() {
    inline fun showAlert(
        titleResId: Int? = null,
        msgResId: Int,
        crossinline onClosed: () -> Unit
    ) = (requireActivity() as Activity).showAlert(titleResId, msgResId, onClosed)

    inline fun showAlert(
        title: String? = null,
        msg: String,
        crossinline onClosed: () -> Unit
    ) = (requireActivity() as Activity).showAlert(title, msg, onClosed)

    inline fun showConfirm(
        titleResId: Int? = null,
        msgResId: Int,
        crossinline onConfirm: (Boolean) -> Unit
    ) = (requireActivity() as Activity).showConfirm(titleResId, msgResId, onConfirm)

    inline fun showConfirm(
        title: String? = null,
        msg: String,
        crossinline onConfirm: (Boolean) -> Unit
    ) = (requireActivity() as Activity).showConfirm(title, msg, onConfirm)
}