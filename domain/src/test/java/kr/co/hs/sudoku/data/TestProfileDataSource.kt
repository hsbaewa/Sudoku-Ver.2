package kr.co.hs.sudoku.data

import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import java.util.Date
import javax.inject.Inject

class TestProfileDataSource
@Inject constructor() {
    val dummyData = hashMapOf<String, ProfileEntity?>(
        "0" to object : ProfileEntity.UserEntity {
            override val lastCheckedAt: Date = Date()
            override val uid: String = "0"
            override var displayName: String = "user0"
            override var message: String? = "message0"
            override var iconUrl: String? = "https://cdn-icons-png.flaticon.com/512/21/21104.png"
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        },
        "1" to object : ProfileEntity.UserEntity {
            override val lastCheckedAt: Date = Date()
            override val uid: String = "1"
            override var displayName: String = "user1"
            override var message: String? = "message1"
            override var iconUrl: String? = null
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        },
        "2" to object : ProfileEntity.OnlineUserEntity {
            override val checkedAt: Date = Date()
            override val uid: String = "2"
            override var displayName: String = "user2"
            override var message: String? = "message2"
            override var iconUrl: String? = "https://cdn-icons-png.flaticon.com/512/21/21104.png"
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        }
    )
}