package kr.co.hs.sudoku.feature.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.di.repositories.AdminRepositoryQualifier
import kr.co.hs.sudoku.model.admin.AdminPermissionEntity
import kr.co.hs.sudoku.repository.admin.AdminPermissionRepository
import kr.co.hs.sudoku.viewmodel.ViewModel
import javax.inject.Inject

@HiltViewModel
class AdminViewModel
@Inject constructor(
    @AdminRepositoryQualifier
    private val repository: AdminPermissionRepository
) : ViewModel() {
    private val _adminPermission = MutableLiveData<AdminPermissionEntity>()
    val adminPermission: LiveData<AdminPermissionEntity> by this::_adminPermission

    init {
        _adminPermission.value = AdminPermissionEntity(
            hasPermissionCreateChallenge = false,
            hasPermissionAppUpdatePush = false
        )
    }

    fun requestAdminPermission(uid: String) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val permission = withContext(Dispatchers.IO) { repository.getPermission(uid) }
            _adminPermission.value = permission
            setProgress(false)
        }
}