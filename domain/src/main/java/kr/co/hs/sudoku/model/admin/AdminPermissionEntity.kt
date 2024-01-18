package kr.co.hs.sudoku.model.admin

data class AdminPermissionEntity(
    val hasPermissionCreateChallenge: Boolean,
    val hasPermissionAppUpdatePush: Boolean
)