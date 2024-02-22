package kr.co.hs.sudoku.feature.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class OnlineUserListViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    class ProviderFactory(
        private val profileRepository: ProfileRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(OnlineUserListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                OnlineUserListViewModel(profileRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private val _onlineUserList = MutableLiveData<List<ProfileEntity.OnlineUserEntity>>()
    val onlineUserList: LiveData<List<ProfileEntity.OnlineUserEntity>> by this::_onlineUserList

    fun requestOnlineUserList() = viewModelScope.launch(viewModelScopeExceptionHandler) {
        setProgress(true)
        with(profileRepository) {
            runCatching { withContext(kotlinx.coroutines.Dispatchers.IO) { getOnlineUserList() } }
                .getOrNull()
                ?.run { _onlineUserList.value = this }
        }
        setProgress(false)
    }
}