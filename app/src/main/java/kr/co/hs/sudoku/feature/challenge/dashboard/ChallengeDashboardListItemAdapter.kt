package kr.co.hs.sudoku.feature.challenge.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.ListAdapter
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeButtonBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeHeaderBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeMatrixBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeMyRankBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeRankBinding
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeTitleBinding
import kr.co.hs.sudoku.model.challenge.ChallengeEntity

class ChallengeDashboardListItemAdapter(
    private val onClickStart: (ChallengeEntity) -> Unit,
    private val onClickSelectDate: () -> Unit,
    private val onClickShowProfile: (uid: String) -> Boolean
) : ListAdapter<ChallengeDashboardListItem, ChallengeDashboardListItemViewHolder<out ChallengeDashboardListItem>>(
    ChallengeDashboardListItemDiffCallback()
) {
    companion object {
        private const val VT_TITLE = 100
        private const val VT_MATRIX_HEADER = 101
        private const val VT_MATRIX = 102
        private const val VT_RANK_HEADER = 103
        private const val VT_RANKER = 104
        private const val VT_BUTTON = 105
        private const val VT_MY_RANK = 106
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChallengeDashboardListItemViewHolder<out ChallengeDashboardListItem> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_TITLE -> ChallengeDashboardListItemViewHolder.Title(
                LayoutListItemChallengeTitleBinding.inflate(inflater, parent, false)
            ).apply {
                binding.btnSelectDate.setOnClickListener { onClickSelectDate() }
            }

            VT_MATRIX_HEADER -> ChallengeDashboardListItemViewHolder.MatrixHeader(
                LayoutListItemChallengeHeaderBinding.inflate(inflater, parent, false)
            )

            VT_MATRIX -> ChallengeDashboardListItemViewHolder.MatrixInfo(
                LayoutListItemChallengeMatrixBinding.inflate(inflater, parent, false)
            )

            VT_BUTTON -> ChallengeDashboardListItemViewHolder.Button(
                LayoutListItemChallengeButtonBinding.inflate(inflater, parent, false)
            ).apply {
                setOnClickListener {
                    onClickStart((getItem(bindingAdapterPosition) as ChallengeDashboardListItem.ChallengeStartItem).challengeEntity)
                }
            }

            VT_MY_RANK -> ChallengeDashboardListItemViewHolder.MyRank(
                LayoutListItemChallengeMyRankBinding.inflate(inflater, parent, false)
            )

            VT_RANK_HEADER -> ChallengeDashboardListItemViewHolder.RankHeader(
                LayoutListItemChallengeHeaderBinding.inflate(inflater, parent, false)
            )

            else -> ChallengeDashboardListItemViewHolder.Rank(
                LayoutListItemChallengeRankBinding.inflate(inflater, parent, false)
            ).apply {
                clickableView.setOnClickListener {
                    val uid = getItem(bindingAdapterPosition).id
                    ProfilePopupMenu(it.context, it).show(uid, onClickShowProfile)
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: ChallengeDashboardListItemViewHolder<out ChallengeDashboardListItem>,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is ChallengeDashboardListItem.ChallengeStartItem ->
                (holder as ChallengeDashboardListItemViewHolder.Button).onBind(item)

            is ChallengeDashboardListItem.MatrixItem ->
                (holder as ChallengeDashboardListItemViewHolder.MatrixInfo).onBind(item)

            is ChallengeDashboardListItem.RankItem ->
                (holder as ChallengeDashboardListItemViewHolder.Rank).onBind(item)

            is ChallengeDashboardListItem.MyRankItem ->
                (holder as ChallengeDashboardListItemViewHolder.MyRank).onBind(item)

            is ChallengeDashboardListItem.TitleItem ->
                (holder as ChallengeDashboardListItemViewHolder.Title).onBind(item)

            is ChallengeDashboardListItem.MatrixHeaderItem ->
                (holder as ChallengeDashboardListItemViewHolder.MatrixHeader).onBind(item)

            is ChallengeDashboardListItem.RankHeaderItem ->
                (holder as ChallengeDashboardListItemViewHolder.RankHeader).onBind(item)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ChallengeDashboardListItem.ChallengeStartItem -> VT_BUTTON
        is ChallengeDashboardListItem.MatrixItem -> VT_MATRIX
        is ChallengeDashboardListItem.RankItem -> VT_RANKER
        is ChallengeDashboardListItem.TitleItem -> VT_TITLE
        is ChallengeDashboardListItem.MyRankItem -> VT_MY_RANK
        is ChallengeDashboardListItem.MatrixHeaderItem -> VT_MATRIX_HEADER
        is ChallengeDashboardListItem.RankHeaderItem -> VT_RANK_HEADER
    }

    override fun onViewRecycled(holder: ChallengeDashboardListItemViewHolder<out ChallengeDashboardListItem>) {
        super.onViewRecycled(holder)
        holder.onRecycled()
    }

    private class ProfilePopupMenu(
        context: Context, anchor: View
    ) : PopupMenu(ContextThemeWrapper(context, R.style.Theme_HSSudoku2), anchor),
        PopupMenu.OnMenuItemClickListener {
        var onClickShowProfile: ((String) -> Boolean)? = null
        var uid = ""

        fun show(uid: String, onClickShowProfile: (String) -> Boolean) {
            inflate(R.menu.profile)
            this.uid = uid
            this.onClickShowProfile = onClickShowProfile
            setOnMenuItemClickListener(this)
            show()
        }

        override fun onMenuItemClick(item: MenuItem?) = when (item?.itemId) {
            R.id.profile -> onClickShowProfile?.invoke(uid) ?: false
            else -> false
        }
    }
}