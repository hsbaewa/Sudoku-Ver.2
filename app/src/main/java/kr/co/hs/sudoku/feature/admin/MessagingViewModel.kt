package kr.co.hs.sudoku.feature.admin

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.feature.messaging.MessagingManager
import kr.co.hs.sudoku.viewmodel.ViewModel

class MessagingViewModel(
    private val messagingManager: MessagingManager
) : ViewModel() {
    class ProviderFactory(
        private val messagingManager: MessagingManager
    ) : ViewModelProvider.Factory {

        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MessagingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                MessagingViewModel(messagingManager) as T
            } else {
                throw Exception("unknown view-model class")
            }
        }
    }

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