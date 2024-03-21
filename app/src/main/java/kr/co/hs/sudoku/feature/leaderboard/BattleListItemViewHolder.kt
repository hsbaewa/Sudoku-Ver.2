package kr.co.hs.sudoku.feature.leaderboard

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import coil.request.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemLeaderboardBinding
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity
import kr.co.hs.sudoku.usecase.UseCase
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase

class BattleListItemViewHolder(
    binding: LayoutListItemLeaderboardBinding,
    private val getProfile: GetProfileUseCase
) : LeaderBoardListItemViewHolder<LeaderBoardListItem.BattleItem>(binding) {

    private var requestProfileJob: Job? = null
    private var disposableProfileIcon: Disposable? = null

    private var entity: BattleLeaderBoardEntity? = null
    private var isMine: Boolean = false

    override fun onBind(item: LeaderBoardListItem) {
        when (item) {
            is LeaderBoardListItem.BattleItem -> {
                entity = item.entity
                isMine = item.isMine
                setGrade(item.grade)
                setRecord(
                    itemView.context.getString(
                        R.string.format_statistics,
                        item.entity.winCount,
                        item.entity.playCount - item.entity.winCount
                    )
                )
                tvDivider.isVisible = false
            }

            is LeaderBoardListItem.BattleItemForMine -> {
                entity = item.entity
                isMine = true
                setGrade(item.grade)
                setRecord(
                    itemView.context.getString(
                        R.string.format_statistics,
                        item.entity.winCount,
                        item.entity.playCount - item.entity.winCount
                    )
                )
                tvDivider.isVisible = true
            }

            is LeaderBoardListItem.Empty -> {
                entity = null
                isMine = false
                setGrade(item.grade)
                setRecord(null)
                setProfile(null)
                tvDivider.isVisible = false
            }

            else -> {}
        }
    }

    override fun onViewAttachedToWindow() {
        requestProfileJob = itemView.findViewTreeLifecycleOwner()?.lifecycleScope
            ?.launch {
                val uid = entity?.uid ?: return@launch
                callbackFlow {
                    getProfile(uid, this) {
                        when (it) {
                            is UseCase.Result.Error -> when (it.e) {
                                GetProfileUseCase.EmptyUserId ->
                                    close(IllegalArgumentException("user id is empty"))

                                GetProfileUseCase.ProfileNotFound ->
                                    close(NullPointerException("profile not found"))
                            }

                            is UseCase.Result.Exception -> close(it.t)
                            is UseCase.Result.Success -> launch {
                                send(it.data)
                                close()
                            }
                        }
                    }

                    awaitClose()
                }
                    .catch { cardView.visibility = View.INVISIBLE }
                    .collect {
                        disposableProfileIcon = setProfile(it, isMine)
                        cardView.visibility = View.VISIBLE
                    }
            }
    }

    override fun onViewDetachedFromWindow() {}

    override fun onViewRecycled() {
        requestProfileJob?.cancel()
        disposableProfileIcon?.dispose()
    }
}