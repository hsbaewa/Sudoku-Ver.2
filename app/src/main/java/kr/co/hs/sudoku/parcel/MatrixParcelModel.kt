package kr.co.hs.sudoku.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MatrixParcelModel(val matrix: List<List<Int>>) : Parcelable