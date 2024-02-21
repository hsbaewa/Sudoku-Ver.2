package kr.co.hs.sudoku.feature.profile

import kr.co.hs.sudoku.model.logs.ChallengeClearLogEntity
import java.util.Date

sealed interface ProfileItem {
    val id: String

    data class Icon(val url: String) : ProfileItem {
        override val id: String
            get() = "url=$url"
    }

    data class DisplayName(val name: String, val nationFlag: String?) : ProfileItem {
        override val id: String
            get() = "name=$name, nationFlag=$nationFlag"
    }

    data class Message(val message: String) : ProfileItem {
        override val id: String
            get() = "message=$message"
    }

    data class LastChecked(val date: Date) : ProfileItem {
        override val id: String
            get() = "date=$date"
    }

    data class BattleLadder(val playCount: Long, val winCount: Long, val ranking: Long) :
        ProfileItem {
        override val id: String
            get() = "playCount=$playCount, winCount=$winCount, ranking=$ranking"
    }

    data class Divider(val type: DividerType) : ProfileItem {
        override val id: String = "divider_${type.name}"
    }

    enum class DividerType { Challenge }

    data class ChallengeLog(val item: ChallengeClearLogEntity) : ProfileItem {
        override val id: String
            get() = item.id
    }
}