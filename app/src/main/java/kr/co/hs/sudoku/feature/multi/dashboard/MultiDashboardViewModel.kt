package kr.co.hs.sudoku.feature.multi.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.liveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleStatisticsEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class MultiDashboardViewModel(
    val battleRepository: BattleRepository
) : ViewModel() {
    class ProviderFactory(
        private val battleRepository: BattleRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MultiDashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                MultiDashboardViewModel(battleRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _currentMultiPlay = MutableLiveData<BattleEntity?>()
    val currentMultiPlay: LiveData<BattleEntity?> by this::_currentMultiPlay

    val multiPlayPagingData: LiveData<PagingData<MultiDashboardListItem>>
        get() = Pager(
            config = PagingConfig(pageSize = 1),
            pagingSourceFactory = {
                BattleListPagingSource(battleRepository)
            }
        ).liveData.cachedIn(viewModelScope)

    private class BattleListPagingSource(
        private val battleRepository: BattleRepository
    ) : PagingSource<Long, MultiDashboardListItem>() {
        override fun getRefreshKey(state: PagingState<Long, MultiDashboardListItem>) = null
        override suspend fun load(params: LoadParams<Long>) = runCatching {
            val list = buildList {
                add(MultiDashboardListItem.TitleItem(""))
                add(MultiDashboardListItem.HeaderUsersItem)
                val participating = withContext(Dispatchers.IO) {
                    battleRepository.runCatching { getParticipating() }.getOrNull()
                }
                if (participating == null) {
                    add(MultiDashboardListItem.CreateNewItem)
                } else {
                    add(MultiDashboardListItem.MultiPlayItem(participating, true))
                }
                add(MultiDashboardListItem.HeaderOthersItem)
                val remain = withContext(Dispatchers.IO) { battleRepository.list() }
                addAll(
                    remain.mapNotNull { item ->
                        item.takeIf { it != participating }
                            ?.run { MultiDashboardListItem.MultiPlayItem(this, false) }
                    }
                )
            }
            val nextKey: Long? = null
            LoadResult.Page(list, null, nextKey)
        }.getOrElse { LoadResult.Error(it) }
    }


    inline fun requestParticipant(
        entity: BattleEntity,
        crossinline onStatus: (RequestStatus<BattleEntity>) -> Unit
    ) = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        onStatus(OnError(throwable))
    }) {
        onStatus(OnStart())
        withContext(Dispatchers.IO) { battleRepository.getParticipants(entity) }
        onStatus(OnFinish(entity))
    }

    inline fun requestStatistics(
        entity: ParticipantEntity,
        crossinline onStatus: (RequestStatus<BattleStatisticsEntity>) -> Unit
    ) = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        onStatus(OnError(throwable))
    }) {
        onStatus(OnStart())
        val stat = withContext(Dispatchers.IO) { battleRepository.getStatistics(entity.uid) }
        onStatus(OnFinish(stat))
    }

    fun checkCurrentMultiPlay() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val entity = withContext(Dispatchers.IO) {
            battleRepository.runCatching { getParticipating() }.getOrNull()
        }
        _currentMultiPlay.value = entity
        setProgress(false)
    }
}