package kr.co.hs.sudoku.feature.profile

import android.text.Spannable
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.databinding.LayoutListItemChallengeClearLogBinding
import kr.co.hs.sudoku.extension.NumberExtension.toTimerFormat
import kr.co.hs.sudoku.extension.platform.ContextExtension.getColorCompat
import java.text.SimpleDateFormat
import java.util.Locale

class ChallengeLogItemViewHolder(val binding: LayoutListItemChallengeClearLogBinding) :
    ProfileItemViewHolder(binding.root) {
    override fun onBind(item: ProfileItem?) {
        when (item) {
            is ProfileItem.ChallengeLog -> onBindChallengeLog(item)
            else -> {}
        }
    }

    private fun onBindChallengeLog(item: ProfileItem.ChallengeLog) {
        with(binding.tvLog) {
            val challengeCreatedAt = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(item.item.createdAt)

            val record = item.item.record.toTimerFormat()
            val grade: String
            val gradeColor: Int
            when (item.item.grade) {
                1L -> {
                    grade = getString(R.string.rank_format_first)
                    gradeColor = getColorCompat(R.color.gold)
                }

                2L -> {
                    grade = getString(R.string.rank_format_second)
                    gradeColor = getColorCompat(R.color.silver)
                }

                3L -> {
                    grade = getString(R.string.rank_format_third)
                    gradeColor = getColorCompat(R.color.bronze)
                }

                else -> {
                    grade = context.getString(R.string.rank_format, item.item.grade)
                    gradeColor = getColorCompat(R.color.gray_700)
                }
            }

            val clearAt = DateUtils.getRelativeTimeSpanString(
                item.item.clearAt.time,
                System.currentTimeMillis(),
                0L,
                DateUtils.FORMAT_ABBREV_ALL
            )

            text = context.getString(
                R.string.profile_challenge_log_format,
                challengeCreatedAt,
                record,
                grade,
                clearAt
            )

            with(text as Spannable) {
                setSpan(
                    ForegroundColorSpan(context.getColorCompat(R.color.gray_700)),
                    indexOf(challengeCreatedAt),
                    indexOf(challengeCreatedAt) + challengeCreatedAt.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(context.getColorCompat(R.color.gray_700)),
                    indexOf(record),
                    indexOf(record) + record.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(gradeColor),
                    indexOf(grade),
                    indexOf(grade) + grade.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    override fun onViewRecycled() {}
}