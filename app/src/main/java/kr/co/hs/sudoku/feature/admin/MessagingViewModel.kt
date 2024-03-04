package kr.co.hs.sudoku.feature.admin

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.di.MessagingManagerQualifier
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class MessagingViewModel @Inject constructor(
    @MessagingManagerQualifier
    private val messagingManager: MessagingManager
) : ViewModel() {

    fun send(action: MessagingManager.Action, onComplete: (Throwable?) -> Unit) =
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            viewModelScopeExceptionHandler.handleException(coroutineContext, throwable)
            onComplete(throwable)
        }) {
            setProgress(true)
            messagingManager.sendNotification(action)
            setProgress(false)
            onComplete(null)
        }
}