package kr.co.hs.sudoku.feature.multi.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.ad.MultiDashboardAdQualifier
import kr.co.hs.sudoku.di.repositories.BattleRepositoryQualifier
import kr.co.hs.sudoku.feature.ad.NativeItemAdManager
import kr.co.hs.sudoku.model.battle.BattleEntity
import kr.co.hs.sudoku.model.battle.BattleLeaderBoardEntity
import kr.co.hs.sudoku.model.battle.ParticipantEntity
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MultiDashboardViewModel
@Inject
constructor(
    @MultiDashboardAdQualifier
    private val nativeItemAdManager: NativeItemAdManager,
    @BattleRepositoryQualifier
    val battleRepository: BattleRepository
) : ViewModel() {

    private val _currentMultiPlay = MutableLiveData<BattleEntity?>()
    val currentMultiPlay: LiveData<BattleEntity?> by this::_currentMultiPlay

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    val multiPlayPagingData: LiveData<PagingData<MultiDashboardListItem>>
        get() = Pager(
            config = PagingConfig(pageSize = 1),
            pagingSourceFactory = {
                when (val source = filter) {
                    is PossibleToJoinPagingSource -> PossibleToJoinPagingSource(
                        source.battleRepository,
                        source.nativeItemAdManager
                    )

                    is AllPagingSource -> AllPagingSource(
                        source.battleRepository,
                        source.nativeItemAdManager
                    )
                }
            }
        ).liveData.cachedIn(viewModelScope)

    private var filter: Filter = PossibleToJoinPagingSource(battleRepository, nativeItemAdManager)
    fun showOnlyPossibleToJoin(on: Boolean) =
        when (on) {
            true -> PossibleToJoinPagingSource(battleRepository, nativeItemAdManager)
            false -> AllPagingSource(battleRepository, nativeItemAdManager)
        }.apply { filter = this }

    sealed interface Filter

    private abstract class BattleListPagingSource(
        val battleRepository: BattleRepository,
        val nativeItemAdManager: NativeItemAdManager?
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
                add(getFilterItem())
                val currentSize = size

                val remain = withContext(Dispatchers.IO) { getBattleEntities() }
                addAll(
                    remain.map { MultiDashboardListItem.MultiPlayItem(it, false) }
                )

                nativeItemAdManager?.fetchNativeAd()?.run {
                    val random =
                        Random.nextInt(remain.size.plus(1).takeIf { it <= 3 } ?: 3) + currentSize
                    add(random, MultiDashboardListItem.AdItem(this))
                }
            }
            val nextKey: Long? = null
            LoadResult.Page(list, null, nextKey)
        }.getOrElse { LoadResult.Error(it) }

        protected abstract fun getFilterItem(): MultiDashboardListItem.FilterItem
        protected abstract suspend fun getBattleEntities(): List<BattleEntity>
    }

    private class AllPagingSource(
        battleRepository: BattleRepository,
        nativeItemAdManager: NativeItemAdManager?
    ) : BattleListPagingSource(battleRepository, nativeItemAdManager), Filter {
        override fun getFilterItem() = MultiDashboardListItem.FilterItem(false)
        override suspend fun getBattleEntities() = battleRepository
            .runCatching { list() }
            .getOrDefault(emptyList())
    }

    private class PossibleToJoinPagingSource(
        battleRepository: BattleRepository,
        nativeItemAdManager: NativeItemAdManager?
    ) : BattleListPagingSource(battleRepository, nativeItemAdManager), Filter {
        override fun getFilterItem() = MultiDashboardListItem.FilterItem(true)
        override suspend fun getBattleEntities() =
            battleRepository
                .runCatching { possibleToJoinList() }
                .getOrDefault(emptyList())
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
        crossinline onStatus: (RequestStatus<BattleLeaderBoardEntity>) -> Unit
    ) = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        onStatus(OnError(throwable))
    }) {
        onStatus(OnStart())
        val stat = withContext(Dispatchers.IO) { battleRepository.getLeaderBoard(entity.uid) }
        onStatus(OnFinish(stat))
    }

    inline fun requestStatistics(
        uid: String,
        crossinline onStatus: (RequestStatus<BattleLeaderBoardEntity>) -> Unit
    ) = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        onStatus(OnError(throwable))
    }) {
        onStatus(OnStart())
        val stat = withContext(Dispatchers.IO) { battleRepository.getLeaderBoard(uid) }
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

    fun registerRank() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        battleRepository.runCatching { syncLeaderBoard() }.getOrNull()
        setProgress(false)
    }
}