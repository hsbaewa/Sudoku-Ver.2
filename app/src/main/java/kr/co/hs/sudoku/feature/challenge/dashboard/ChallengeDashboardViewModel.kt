package kr.co.hs.sudoku.feature.challenge.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class ChallengeDashboardViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    class ProviderFactory(
        private val repository: ChallengeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ChallengeDashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                ChallengeDashboardViewModel(repository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _dashboardItemList = MutableLiveData<List<ChallengeDashboardListItem>>()
    val dashboardItemList: LiveData<List<ChallengeDashboardListItem>> by this::_dashboardItemList

    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun requestChallengeDashboard() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        val dashboardList = ArrayList<ChallengeDashboardListItem>()
        dashboardList.add(ChallengeDashboardListItem.TitleItem)

        val challenge = withContext(Dispatchers.IO) { repository.getLatestChallenge() }
        dashboardList.add(ChallengeDashboardListItem.MatrixHeaderItem)
        dashboardList.add(ChallengeDashboardListItem.MatrixItem(challenge.matrix))

        dashboardList.add(ChallengeDashboardListItem.RankHeaderItem)
        val records = withContext(Dispatchers.IO) {
            repository.getRecords(10)
        }
        dashboardList.addAll(
            List(10) {
                val rankerEntity = records.runCatching { get(it) }
                    .getOrDefault(RankerEntity("", "-", null, null, null, it + 1L, -1))
                ChallengeDashboardListItem.RankItem(rankerEntity)
            }
        )

        if (dashboardList.find { it is ChallengeDashboardListItem.RankItem && it.rankEntity.uid == currentUserUid } == null) {

            val myRecord = withContext(Dispatchers.IO) {
                currentUserUid?.let { uid ->
                    repository.runCatching { getRecord(uid) }.getOrNull()
                }
            }
            myRecord
                ?.takeIf { it.clearTime >= 0 }
                ?.run { dashboardList.add(ChallengeDashboardListItem.MyRankItem(this)) }
        }

        dashboardList.add(ChallengeDashboardListItem.ChallengeStartItem(challenge))

        _dashboardItemList.value = dashboardList

        setProgress(false)
    }

    suspend fun doDeleteRecord() = withContext(Dispatchers.IO) {
        currentUserUid?.let { repository.deleteRecord(it) }
    }
}