package kr.co.hs.sudoku.feature.challenge.dashboard

import androidx.lifecycle.LiveData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.ad.ChallengeDashboardAdQualifier
import kr.co.hs.sudoku.feature.ad.NativeItemAdManager
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChallengeDashboardViewModel
@Inject constructor(
    private val repository: ChallengeRepository,
    @ChallengeDashboardAdQualifier
    private val nativeItemAdManager: NativeItemAdManager,
) : ViewModel() {

    val challengeDashboardPagingData: LiveData<PagingData<ChallengeDashboardListItem>>
        get() = Pager(
            config = PagingConfig(pageSize = 5, initialLoadSize = 1),
            pagingSourceFactory = {
                ChallengePagingSource(repository, nativeItemAdManager)
            }
        ).liveData.cachedIn(viewModelScope)

    private class ChallengePagingSource(
        private val repository: ChallengeRepository,
        private val nativeItemAdManager: NativeItemAdManager
    ) : PagingSource<Long, ChallengeDashboardListItem>() {
        override fun getRefreshKey(state: PagingState<Long, ChallengeDashboardListItem>) = null
        override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ChallengeDashboardListItem> {
            val isFirst = params.key == null
            val key = params.key?.run { Date(this) } ?: Date()
            val count = params.loadSize.toLong()

            val list = buildList {
                if (isFirst) {
                    add(ChallengeDashboardListItem.TitleItem)
                }
                val originalChallengeList: MutableList<ChallengeDashboardListItem> = repository
                    .runCatching { withContext(Dispatchers.IO) { getChallenges(key, count) } }
                    .getOrDefault(emptyList())
                    .map { ChallengeDashboardListItem.ChallengeItem(it) }
                    .toMutableList()

                withContext(Dispatchers.IO) { nativeItemAdManager.fetchNativeAd() }?.let { ad ->
                    originalChallengeList.add(ChallengeDashboardListItem.AdItem(ad))
                }

                addAll(originalChallengeList)
            }

            val nextKey = list.findLast { it is ChallengeDashboardListItem.ChallengeItem }
                ?.run {
                    if (this is ChallengeDashboardListItem.ChallengeItem) {
                        challengeEntity.createdAt?.time
                    } else null
                }

            return LoadResult.Page(list, null, nextKey)
        }
    }

    suspend fun doDeleteRecord(challengeId: String) = withContext(Dispatchers.IO) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { currentUserUid ->
            repository.deleteRecord(challengeId, currentUserUid)
        }
    }
}