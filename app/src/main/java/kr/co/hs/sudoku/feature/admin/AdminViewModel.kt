package kr.co.hs.sudoku.feature.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.hs.sudoku.model.admin.AdminPermissionEntity
import kr.co.hs.sudoku.repository.admin.AdminPermissionRepository
import kr.co.hs.sudoku.viewmodel.ViewModel

class AdminViewModel(
    private val repository: AdminPermissionRepository
) : ViewModel() {

    class ProviderFactory(
        private val adminPermissionRepository: AdminPermissionRepository
    ) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                AdminViewModel(adminPermissionRepository) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }


    private val _adminPermission = MutableLiveData<AdminPermissionEntity>()
    val adminPermission: LiveData<AdminPermissionEntity> by this::_adminPermission

    init {
        _adminPermission.value = AdminPermissionEntity(false)
    }

    fun requestAdminPermission(uid: String) =
        viewModelScope.launch(viewModelScopeExceptionHandler) {
            setProgress(true)
            val permission = withContext(Dispatchers.IO) { repository.getPermission(uid) }
            _adminPermission.value = permission
            setProgress(false)
        }
}