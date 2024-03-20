package kr.co.hs.sudoku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

abstract class ViewModel : ViewModel() {
    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> by this::_error

    private val _isRunningProgress = MutableLiveData(false)
    val isRunningProgress: LiveData<Boolean> by this::_isRunningProgress
    protected fun setProgress(progress: Boolean) {
        _isRunningProgress.value = progress
    }

    protected val viewModelScopeExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isRunningProgress.value = false
        _error.value = throwable
    }

    protected fun setError(t: Throwable) {
        _error.value = t
    }

    sealed interface RequestStatus<out D>
    class OnStart<D> : RequestStatus<D>
    data class OnFinish<D>(val d: D) : RequestStatus<D>
    data class OnError<D>(val t: Throwable) : RequestStatus<D>
}