package kr.co.hs.sudoku.feature.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.ProfileRepositoryQualifier
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class OnlineUserListViewModel
@Inject constructor(
    @ProfileRepositoryQualifier
    private val profileRepository: ProfileRepository
) : ViewModel() {

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