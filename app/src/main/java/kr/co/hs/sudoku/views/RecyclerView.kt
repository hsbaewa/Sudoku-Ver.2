package kr.co.hs.sudoku.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class RecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun addVerticalDivider(
        thickness: Float? = null,
        colorResId: Int? = null,
        isLastItemDecorated: Boolean? = null,
        insetHorizontal: Float? = null
    ) = addItemDecoration(
        VerticalDividerDecoration(context)
            .also { isLastItemDecorated?.run { it.isLastItemDecorated = this } }
            .also {
                colorResId
                    ?.run { it.color(this) }
                    ?: it.transparent()
            }
            .also { thickness?.run { it.thickness(this) } }
            .also {
                insetHorizontal?.run {
                    it.dividerInsetStart = this.toInt()
                    it.dividerInsetEnd = this.toInt()
                }
            }
    )

    fun addHorizontalDivider(
        thickness: Float? = null,
        colorResId: Int? = null,
        isLastItemDecorated: Boolean? = null
    ) = addItemDecoration(
        HorizontalDividerDecoration(context)
            .also {
                if (isLastItemDecorated == false) {
                    val spanCount = (layoutManager as GridLayoutManager).spanCount
                    it.setSpanCount(spanCount)
                }
            }
            .also {
                colorResId
                    ?.run { it.color(this) }
                    ?: it.transparent()
            }
            .also { thickness?.run { it.thickness(this) } }
    )

    abstract class Adapter<VH : ViewHolder> : RecyclerView.Adapter<VH>()


    // 화면에 보이는 애들만 갱신
    private fun notifyDataSetChangedInVisibleItem() =
        (layoutManager as? LinearLayoutManager)
            ?.run { findFirstVisibleItemPosition() to findLastVisibleItemPosition() }
            ?.run { adapter?.notifyItemRangeChanged(first, second + 1) }


    // This method access the private member mOnItemTouchListeners from recyclerview and remove all the listener.
    // This method is created as an extension of the class RecyclerView
    private fun removeAllTouchListener() {
        runCatching {
            (getPrivateProperty("mOnItemTouchListeners") as? java.util.ArrayList<*>)?.clear()
        }
    }


    // This method use reflection to access a private member of a class
    // variableName : Name of the member to access
    private fun <T : Any> T.getPrivateProperty(variableName: String) =
        javaClass.getDeclaredField(variableName).also { it.isAccessible = true }.get(this)


    val originAdapter: RecyclerView.Adapter<*>?
        get() = adapter?.run {
            when (this) {
//                is ConcatAdapter -> adapters.find { it is PagingDataAdapter<*, *> }
                else -> this
            }
        }
}