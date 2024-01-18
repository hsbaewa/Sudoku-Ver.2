package kr.co.hs.sudoku.model.admin

class AdminModel {
    var enabledCreateChallenge: Boolean = false
    var enabledAppUpdatePush: Boolean = false

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor() {
        enabledCreateChallenge = false
        enabledAppUpdatePush = false
    }
}