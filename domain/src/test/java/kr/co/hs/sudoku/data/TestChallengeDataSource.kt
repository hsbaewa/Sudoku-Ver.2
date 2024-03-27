package kr.co.hs.sudoku.data

import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.matrix.CustomMatrix
import kr.co.hs.sudoku.model.matrix.IntMatrix
import java.util.Date
import javax.inject.Inject

class TestChallengeDataSource
@Inject constructor() {
    val dummyData = hashMapOf(
        "0" to object : ChallengeEntity {
            override val challengeId: String = "0"
            override val matrix: IntMatrix = CustomMatrix(2, 2)
            override val createdAt: Date = Date(0)
            override var isPlaying: Boolean = false
            override var startPlayAt: Date? = null
            override var isComplete: Boolean = false
            override var relatedUid: String? = null
        },
        "1" to object : ChallengeEntity {
            override val challengeId: String = "1"
            override val matrix: IntMatrix = CustomMatrix(3, 3)
            override val createdAt: Date = Date(1)
            override var isPlaying: Boolean = false
            override var startPlayAt: Date? = null
            override var isComplete: Boolean = false
            override var relatedUid: String? = null
        },
        "2" to object : ChallengeEntity {
            override val challengeId: String = "2"
            override val matrix: IntMatrix = CustomMatrix(4, 4)
            override val createdAt: Date = Date(2)
            override var isPlaying: Boolean = false
            override var startPlayAt: Date? = null
            override var isComplete: Boolean = false
            override var relatedUid: String? = null
        }
    )
}