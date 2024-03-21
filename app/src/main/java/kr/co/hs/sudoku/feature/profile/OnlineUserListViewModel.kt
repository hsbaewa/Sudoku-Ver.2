package kr.co.hs.sudoku.feature.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.usecase.UseCase
import kr.co.hs.sudoku.usecase.user.GetOnlineProfileListUseCase
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class OnlineUserListViewModel
@Inject constructor(
    private val getOnlineUserList: GetOnlineProfileListUseCase
) : ViewModel() {

    private val _onlineUserList = MutableLiveData<List<ProfileEntity.OnlineUserEntity>>()
    val onlineUserList: LiveData<List<ProfileEntity.OnlineUserEntity>> by this::_onlineUserList

    fun requestOnlineUserList() {
        setProgress(true)
        getOnlineUserList(viewModelScope) {
            when (it) {
                is UseCase.Result.Error -> when (it.e) {
                }

                is UseCase.Result.Exception -> {
                    setProgress(false)
                    setError(it.t)
                }

                is UseCase.Result.Success -> {
                    setProgress(false)
                    _onlineUserList.value = it.data
                }
            }
        }
    }
}