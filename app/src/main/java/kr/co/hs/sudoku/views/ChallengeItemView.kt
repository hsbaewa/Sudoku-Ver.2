package kr.co.hs.sudoku.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import coil.request.Disposable
import com.google.firebase.auth.FirebaseAuth
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.feature.profile.ProfilePopupMenu
import kr.co.hs.sudoku.model.challenge.ChallengeEntity
import kr.co.hs.sudoku.model.rank.RankerEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import java.text.SimpleDateFormat
import java.util.Locale

class ChallengeItemView : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        inflate(context, R.layout.layout_view_challenge_item, this)
    }

    private val tvTitle: TextView by lazy { findViewById(R.id.tv_title) }
    private val matrixView: MatrixItemView by lazy { findViewById(R.id.matrix) }
    private val cardViewFirstGrade: CardView by lazy { findViewById(R.id.card_view_first_grade) }
    private val profileViewFirstGrade: ProfileView by lazy { findViewById(R.id.profile_view_first_grade) }
    private val cardViewSecondGrade: CardView by lazy { findViewById(R.id.card_view_second_grade) }
    private val profileViewSecondGrade: ProfileView by lazy { findViewById(R.id.profile_view_second_grade) }
    private val cardViewThirdGrade: CardView by lazy { findViewById(R.id.card_view_third_grade) }
    private val profileViewThirdGrade: ProfileView by lazy { findViewById(R.id.profile_view_third_grade) }
    private val btnLeaderBoard: Button by lazy { findViewById(R.id.btn_leader_board) }
    private val tvDescription: TextView by lazy { findViewById(R.id.tv_description) }
    private var disposableProfileFirst: Disposable? = null
    private var disposableProfileSecond: Disposable? = null
    private var disposableProfileThird: Disposable? = null
    private val currentUserUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var onPopupMenuItemClickListener: ProfilePopupMenu.OnPopupMenuItemClickListener? = null

    fun set(entity: ChallengeEntity) {
        val strCreatedAt = entity.createdAt
            ?.run {
                SimpleDateFormat(
                    context.getString(R.string.challenge_list_item_created_at_format),
                    Locale.getDefault()
                ).format(this)
            }
        setTitle(strCreatedAt)

        with(matrixView) {
            setFixedCellValues(entity.matrix)
            invalidate()
        }

        cardViewFirstGrade.setOnClickListener {
            profileViewFirstGrade.currentProfile?.run { onClickProfile(it, this) }
        }
        cardViewSecondGrade.setOnClickListener {
            profileViewSecondGrade.currentProfile?.run { onClickProfile(it, this) }
        }
        cardViewThirdGrade.setOnClickListener {
            profileViewThirdGrade.currentProfile?.run { onClickProfile(it, this) }
        }
    }

    private fun setTitle(title: String?) = with(tvTitle) {
        text = title ?: context.getString(R.string.no_name)
    }

    private fun onClickProfile(parent: View, profileEntity: ProfileEntity) =
        onPopupMenuItemClickListener?.also { listener ->
            profileEntity.uid.takeUnless { it == currentUserUid }
                ?.run { ProfilePopupMenu(parent.context, parent, listener) }
                ?.show(profileEntity)
        }


    fun setOnProfileClickListener(listener: ProfilePopupMenu.OnPopupMenuItemClickListener) {
        this.onPopupMenuItemClickListener = listener
    }

    fun setLeaderBoard(list: List<RankerEntity>) {
        list.getOrNull(0)
            ?.run {
                disposableProfileFirst = profileViewFirstGrade.loadProfile(this)
                profileViewFirstGrade.setTextColor(if (currentUserUid == uid) R.color.black else R.color.gray_600)
                cardViewFirstGrade.isVisible = true
            }
            ?: run { cardViewFirstGrade.isVisible = false }

        list.getOrNull(1)
            ?.run {
                disposableProfileSecond = profileViewSecondGrade.loadProfile(this)
                profileViewSecondGrade.setTextColor(if (currentUserUid == uid) R.color.black else R.color.gray_600)
                cardViewSecondGrade.isVisible = true
            }
            ?: run { cardViewSecondGrade.isVisible = false }

        list.getOrNull(2)
            ?.run {
                disposableProfileThird = profileViewThirdGrade.loadProfile(this)
                profileViewThirdGrade.setTextColor(if (currentUserUid == uid) R.color.black else R.color.gray_600)
                cardViewThirdGrade.isVisible = true
            }
            ?: run { cardViewThirdGrade.isVisible = false }

        btnLeaderBoard.isVisible = list.isNotEmpty()
        tvDescription.isVisible = list.isEmpty()
    }

    private fun ProfileView.loadProfile(profileEntity: ProfileEntity?): Disposable? {
        return profileEntity
            ?.run { load(this) }
            ?: run {
                clear()
                null
            }
    }

    fun dispose() {
        disposableProfileFirst?.dispose()
        disposableProfileSecond?.dispose()
        disposableProfileThird?.dispose()
    }

    fun setOnClickShowLeaderBoard(l: OnClickListener) = btnLeaderBoard.setOnClickListener(l)
}