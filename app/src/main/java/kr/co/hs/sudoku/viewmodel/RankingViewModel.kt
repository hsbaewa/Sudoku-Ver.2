package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.repository.rank.RankingRepository
import kr.co.hs.sudoku.usecase.ranking.GetRankingUseCaseImpl
import kr.co.hs.sudoku.usecase.ranking.GetRecordUseCaseImpl

class RankingViewModel(
    private val repository: RankingRepository,
    private val uid: String
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val repository: RankingRepository,
        private val uid: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.takeIf { it.isAssignableFrom(RankingViewModel::class.java) }
                ?.run { RankingViewModel(repository, uid) as T }
                ?: throw IllegalArgumentException("unKnown ViewModel class")
        }
    }

    private val _top10 = MutableLiveData<List<RankerEntity>>()
    val top10: LiveData<List<RankerEntity>> by this::_top10

    private val _myRecord = MutableLiveData<RankerEntity?>()
    val myRecord: LiveData<RankerEntity?> by this::_myRecord

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> by this::_error

    private val _isRunningProgress = MutableLiveData(false)
    val isRunningProgress: LiveData<Boolean> by this::_isRunningProgress

    fun requestRanking() = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        _isRunningProgress.value = false
        _error.value = throwable
    }) {
        _isRunningProgress.value = true

        val ranking =
            withContext(Dispatchers.IO) { GetRankingUseCaseImpl(repository).invoke().last() }
        _top10.value = ranking
        ranking
            .indexOfFirst { it.uid == uid }
            .takeUnless { it >= 0 }
            ?.run {
                withContext(Dispatchers.IO) { GetRecordUseCaseImpl(repository)(uid).last() }
            }
            ?.run { _myRecord.value = this }

        _isRunningProgress.value = false
    }

}