package kr.co.hs.sudoku.model.battle

import kr.co.hs.sudoku.model.user.LocaleModel
import kr.co.hs.sudoku.model.user.ProfileModel

class BattleParticipantModel() : ProfileModel {
    override var uid: String = ""
    override var name: String = ""
    override var message: String? = null
    override var iconUrl: String? = null
    override var locale: LocaleModel? = null

    var battleId: String? = null
    var matrix: List<Int>? = null
    var clearTime: Long? = null

    @field:JvmField
    var isReady = false

    constructor(profileModel: ProfileModel) : this() {
        profileModel.let {
            this.uid = it.uid
            this.name = it.name
            this.message = it.message
            this.iconUrl = it.iconUrl
            this.locale = it.locale
        }
    }

    constructor(uid: String) : this() {
        this.uid = uid
    }

    fun update(profileModel: ProfileModel) {
        profileModel.let {
            this.name = it.name
            this.message = it.message
            this.iconUrl = it.iconUrl
            this.locale = it.locale
        }

        battleId = null
        matrix = null
        clearTime = null
        isReady = false
    }
}